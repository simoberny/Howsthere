package it.howsthere.howsthere2.ui.result;

import java.util.List;
import it.howsthere.howsthere2.objects.Constants;
import it.howsthere.howsthere2.objects.Position;

final class MoonChartSamples {
    private MoonChartSamples() { }

    static List<Position> selectedDay(List<Position> samples) {
        int from = Math.min(Constants.MOON_SAMPLES_PER_DAY, samples.size());
        int to = Math.min(2 * Constants.MOON_SAMPLES_PER_DAY, samples.size());
        return samples.subList(from, to);
    }
}
