package it.howsthere.howsthere2.ui.result;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.util.Calendar;

import it.howsthere.howsthere2.R;
import it.howsthere.howsthere2.objects.Panorama;
import it.howsthere.howsthere2.objects.Position;

final class CalendarEvent {
    private CalendarEvent() { }

    static void insert(Context context, Panorama panorama, Position position, @StringRes int title) {
        if (position == null) {
            Toast.makeText(context, R.string.unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(panorama.date);
        calendar.set(Calendar.HOUR_OF_DAY, position.hour);
        calendar.set(Calendar.MINUTE, position.minutes);
        long start = calendar.getTimeInMillis();

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setDataAndType(CalendarContract.Events.CONTENT_URI, "vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, context.getString(title));
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, panorama.lat + ", " + panorama.lon);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start + 60 * 60 * 1000L);
        context.startActivity(intent);
    }
}
