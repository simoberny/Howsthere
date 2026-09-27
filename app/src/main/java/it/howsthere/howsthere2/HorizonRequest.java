package it.howsthere.howsthere2;

import java.io.IOException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

/** Coordinates two independent, single-use IDs. All callbacks run on the main thread. */
final class HorizonRequest {
    private static final int MAX_RETRIES = 3;
    private static final int MAX_STATUS_RETRIES = 8;
    private static final long INITIAL_WAIT_MS = 8_000;

    interface Api {
        @GET("api/query")
        Call<ResponseBody> getId(@Query("lat") double lat, @Query("lon") double lon);
        @GET("api/ready")
        Call<ResponseBody> getStatus(@Query("id") String id);
        @GET("api/horizon.csv?resolution=.999")
        Call<ResponseBody> getProfile(@Query("id") String id);
        @GET("api/horizon-peaks")
        Call<ResponseBody> getNames(@Query("id") String id);
    }

    enum Stage { ID, WAITING, PROFILE, NAMES }

    interface Scheduler {
        void postDelayed(Runnable action, long delayMs);
    }

    interface Listener {
        void onStage(Stage stage);
        void onComplete(String profileId, String profile, String names);
        void onError();
    }

    private final Api api;
    private final Scheduler scheduler;
    private final Listener listener;
    private final double latitude;
    private final double longitude;
    private final Flow profile = new Flow(false);
    private final Flow names = new Flow(true);
    private boolean active;
    private boolean started;

    HorizonRequest(Api api, Scheduler scheduler, Listener listener, double latitude, double longitude) {
        this.api = api;
        this.scheduler = scheduler;
        this.listener = listener;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    void start() {
        if (started) return;
        started = true;
        active = true;
        obtainId(profile);
        obtainId(names);
    }

    void cancel() {
        active = false;
        if (profile.call != null) profile.call.cancel();
        if (names.call != null) names.call.cancel();
    }

    private void obtainId(Flow flow) {
        if (!active) return;
        flow.id = null;
        listener.onStage(Stage.ID);
        enqueue(flow, api.getId(latitude, longitude), text -> {
            flow.id = text.trim();
            schedule(() -> checkStatus(flow, 0), INITIAL_WAIT_MS);
        }, () -> restart(flow));
    }

    private void checkStatus(Flow flow, int retry) {
        if (!active) return;
        listener.onStage(Stage.WAITING);
        Runnable retryStatus = () -> {
            if (retry >= MAX_STATUS_RETRIES) restart(flow);
            else schedule(() -> checkStatus(flow, retry + 1), retryDelay(retry));
        };
        enqueue(flow, api.getStatus(flow.id), text -> {
            if (text.trim().startsWith("1")) download(flow);
            else retryStatus.run();
        }, retryStatus);
    }

    private void download(Flow flow) {
        if (!active) return;
        listener.onStage(flow.optional ? Stage.NAMES : Stage.PROFILE);
        // A download consumes its ID even when its response is lost or unusable.
        // Never poll or download again with that ID; restart from api/query instead.
        Call<ResponseBody> call = flow.optional ? api.getNames(flow.id) : api.getProfile(flow.id);
        enqueue(flow, call, text -> {
            String lower = text.toLowerCase(Locale.ROOT);
            if (lower.contains("not ready") || lower.contains("non erano ancora pronti")) {
                restart(flow);
                return;
            }
            if (!flow.optional) {
                try {
                    HorizonData.parseProfile(text);
                } catch (IllegalArgumentException invalidProfile) {
                    restart(flow);
                    return;
                }
            }
            flow.data = text;
            flow.done = true;
            completeIfReady();
        }, () -> restart(flow));
    }

    private void restart(Flow flow) {
        flow.id = null;
        if (flow.retries >= MAX_RETRIES) {
            if (flow.optional) {
                flow.done = true;
                completeIfReady();
            } else {
                cancel();
                listener.onError();
            }
            return;
        }
        schedule(() -> obtainId(flow), retryDelay(flow.retries++));
    }

    private void completeIfReady() {
        if (!active || !profile.done || !names.done) return;
        active = false;
        listener.onComplete(profile.id, profile.data, names.data);
    }

    private void schedule(Runnable action, long delayMs) {
        scheduler.postDelayed(() -> {
            if (active) action.run();
        }, delayMs);
    }

    private static long retryDelay(int retry) {
        return Math.min(30_000L, 5_000L * (retry + 1));
    }

    private interface TextCallback {
        void accept(String text);
    }

    private void enqueue(Flow flow, Call<ResponseBody> call, TextCallback success, Runnable failure) {
        flow.call = call;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> completed, Response<ResponseBody> response) {
                String text = null;
                try (ResponseBody body = response.body(); ResponseBody error = response.errorBody()) {
                    if (active && response.isSuccessful() && body != null) text = body.string();
                } catch (IOException ignored) {
                    // A body read failure follows the same retry path as a network error.
                }
                if (!active) return;
                flow.call = null;
                if (text == null || text.trim().isEmpty()) failure.run();
                else success.accept(text);
            }

            @Override
            public void onFailure(Call<ResponseBody> failed, Throwable error) {
                if (!active) return;
                flow.call = null;
                failure.run();
            }
        });
    }

    private static final class Flow {
        final boolean optional;
        String id;
        String data = "";
        int retries;
        boolean done;
        Call<ResponseBody> call;

        Flow(boolean optional) {
            this.optional = optional;
        }
    }
}
