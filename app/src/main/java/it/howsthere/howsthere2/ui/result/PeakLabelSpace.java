package it.howsthere.howsthere2.ui.result;

/** Screen-space clearance above the linear mountain profile. Y increases downwards. */
final class PeakLabelSpace {
    private PeakLabelSpace() { }

    static float bottom(float peakY, float[] profile, float left, float right, float gap) {
        float skyline = peakY;
        for (int i = 0; i + 3 < profile.length; i += 2) {
            float x1 = profile[i], y1 = profile[i + 1];
            float x2 = profile[i + 2], y2 = profile[i + 3];
            if (!Float.isFinite(x1) || !Float.isFinite(y1)
                    || !Float.isFinite(x2) || !Float.isFinite(y2)) continue;
            if (Math.max(x1, x2) < left || Math.min(x1, x2) > right) continue;
            // Conservatively reserve clearance for both ends of each intersecting segment.
            skyline = Math.min(skyline, Math.min(y1, y2));
        }
        return skyline - gap;
    }
}
