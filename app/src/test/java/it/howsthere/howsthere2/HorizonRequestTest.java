package it.howsthere.howsthere2;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorizonRequestTest {
    private final FakeApi api = new FakeApi();
    private final Queue<Runnable> scheduled = new ArrayDeque<>();
    private int completed;
    private int errors;
    private String resultNames;
    private String resultId;
    private final HorizonRequest request = new HorizonRequest(api, (r, delay) -> scheduled.add(r),
            new HorizonRequest.Listener() {
                public void onStage(HorizonRequest.Stage stage) { }
                public void onComplete(String id, String profile, String names) {
                    completed++;
                    resultId = id;
                    resultNames = names;
                }
                public void onError() { errors++; }
            }, 45, 9);

    @Test public void usesSeparateIdsAndWaitsForBothDownloads() {
        ready();
        assertEquals("profile-id", api.calls.get(4).id);
        assertEquals("names-id", api.calls.get(5).id);
        api.calls.get(5).ok("10 20 30 40 Monte Test");
        assertEquals(0, completed);
        api.calls.get(4).ok(profile());
        assertEquals(1, completed);
        assertEquals("profile-id", resultId);
        assertEquals("10 20 30 40 Monte Test", resultNames);
        request.start();
        assertEquals(6, api.calls.size());
    }

    @Test public void failedDownloadObtainsFreshIdBeforeRetrying() {
        ready();
        api.calls.get(4).fail();
        scheduled.remove().run();
        assertEquals("id", api.last().kind);
        api.last().ok("replacement-id");
        scheduled.remove().run();
        assertEquals("replacement-id", api.last().id);
        api.last().ok("1");
        assertEquals("profile", api.last().kind);
        assertEquals("replacement-id", api.last().id);
        assertEquals(1, api.calls.stream().filter(c -> c.kind.equals("profile") && c.id.equals("profile-id")).count());
    }

    @Test public void namesDownloadErrorNeverReusesConsumedId() {
        ready();
        api.calls.get(4).ok(profile());
        api.calls.get(5).ok("not ready");
        scheduled.remove().run();
        assertEquals("id", api.last().kind);
        api.last().ok("new-names-id");
        scheduled.remove().run();
        api.last().ok("1");
        assertEquals("names", api.last().kind);
        assertEquals("new-names-id", api.last().id);
        api.last().ok("10 20 30 40 Monte Test");
        assertEquals(1, completed);
        assertEquals(1, api.calls.stream().filter(c -> c.kind.equals("profile")).count());
    }

    @Test public void namesIdFailureDoesNotRestartProfileFlow() {
        request.start();
        api.calls.get(0).ok("profile-id");
        api.calls.get(1).fail();
        scheduled.remove().run(); // Profile status.
        scheduled.remove().run(); // Names ID retry.
        assertEquals("status", api.calls.get(2).kind);
        assertEquals("profile-id", api.calls.get(2).id);
        assertEquals("id", api.calls.get(3).kind);
        api.calls.get(3).ok("names-id");
        scheduled.remove().run();
        assertEquals("names-id", api.last().id);
    }

    @Test public void httpErrorsAndNullBodiesAreRetried() {
        request.start();
        api.calls.get(0).respond(Response.error(503, ResponseBody.create(null, "unavailable")));
        api.calls.get(1).respond(Response.success(null));
        assertEquals(2, scheduled.size());
        scheduled.remove().run();
        scheduled.remove().run();
        assertEquals("id", api.calls.get(2).kind);
        assertEquals("id", api.calls.get(3).kind);
    }

    @Test public void missingOptionalNamesStillCompletesWithValidProfile() {
        ready();
        api.calls.get(4).ok(profile());
        api.calls.get(5).fail();
        for (int i = 0; i < 3; i++) {
            scheduled.remove().run();
            api.last().fail();
        }
        assertEquals(1, completed);
        assertEquals("", resultNames);
        assertEquals(0, errors);
    }

    @Test public void requiredProfileFailureIsBoundedAndCancelsNames() {
        request.start();
        api.calls.get(0).fail();
        for (int i = 0; i < 3; i++) {
            scheduled.remove().run();
            api.last().fail();
        }
        assertEquals(1, errors);
        assertEquals(0, completed);
        assertTrue(api.calls.get(1).cancelled);
        assertTrue(scheduled.isEmpty());
    }

    @Test public void statusPollingIsBoundedAndStartsWithNewIdAfterTimeout() {
        request.start();
        api.calls.get(0).ok("profile-id");
        for (int i = 0; i < 9; i++) {
            scheduled.remove().run();
            assertEquals("status", api.last().kind);
            api.last().ok("0");
        }
        scheduled.remove().run();
        assertEquals("id", api.last().kind);
    }

    @Test public void invalidProfileRestartsInsteadOfProcessing() {
        ready();
        api.calls.get(4).ok("<html>Server error</html>");
        assertEquals(0, completed);
        scheduled.remove().run();
        assertEquals("id", api.last().kind);
    }

    @Test public void cancellationIgnoresPendingCallbacksAndDelayedActions() {
        request.start();
        api.calls.get(0).ok("profile-id");
        request.cancel();
        api.calls.get(1).ok("late-names-id");
        while (!scheduled.isEmpty()) scheduled.remove().run();
        assertEquals(2, api.calls.size());
        assertEquals(0, errors);
        assertEquals(0, completed);
    }

    private void ready() {
        request.start();
        assertEquals(2, api.calls.size());
        api.calls.get(0).ok("profile-id");
        api.calls.get(1).ok("names-id");
        scheduled.remove().run();
        scheduled.remove().run();
        api.calls.get(2).ok("1");
        api.calls.get(3).ok("1");
    }

    static String profile() {
        StringBuilder csv = new StringBuilder("header\n");
        for (int i = 0; i < 360; i++) csv.append(i).append(",0,10,1000,45,9,2000\n");
        return csv.toString();
    }

    private static class FakeApi implements HorizonRequest.Api {
        final List<FakeCall> calls = new ArrayList<>();
        private Call<ResponseBody> add(String kind, String id) {
            FakeCall call = new FakeCall(kind, id);
            calls.add(call);
            return call;
        }
        FakeCall last() { return calls.get(calls.size() - 1); }
        public Call<ResponseBody> getId(double lat, double lon) { return add("id", null); }
        public Call<ResponseBody> getStatus(String id) { return add("status", id); }
        public Call<ResponseBody> getProfile(String id) { return add("profile", id); }
        public Call<ResponseBody> getNames(String id) { return add("names", id); }
    }

    private static class FakeCall implements Call<ResponseBody> {
        final String kind;
        final String id;
        Callback<ResponseBody> callback;
        boolean cancelled;
        FakeCall(String kind, String id) { this.kind = kind; this.id = id; }
        void ok(String text) { respond(Response.success(ResponseBody.create(null, text))); }
        void respond(Response<ResponseBody> response) { callback.onResponse(this, response); }
        void fail() { callback.onFailure(this, new IOException("Disconnected")); }
        public void enqueue(Callback<ResponseBody> callback) { this.callback = callback; }
        public Response<ResponseBody> execute() { throw new UnsupportedOperationException(); }
        public boolean isExecuted() { return callback != null; }
        public void cancel() { cancelled = true; }
        public boolean isCanceled() { return cancelled; }
        public Call<ResponseBody> clone() { return new FakeCall(kind, id); }
        public Request request() { return new Request.Builder().url("https://example.com/").build(); }
        public Timeout timeout() { return new Timeout(); }
    }
}
