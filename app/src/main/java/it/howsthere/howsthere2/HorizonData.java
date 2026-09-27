package it.howsthere.howsthere2;

import it.howsthere.howsthere2.objects.Position;

/** Validates the 360 samples expected by the panorama calculations before modifying a panorama. */
final class HorizonData {
    private HorizonData() { }

    static double[][] parseProfile(String csv) {
        if (csv == null) throw new IllegalArgumentException("Missing horizon profile");
        String[] lines = csv.trim().split("[\\r\\n]+");
        if (lines.length != 361) throw new IllegalArgumentException("Expected 360 horizon samples");
        double[][] result = new double[7][360];
        for (int row = 1; row < lines.length; row++) {
            String[] columns = lines[row].split(",", -1);
            if (columns.length < 7) throw new IllegalArgumentException("Incomplete horizon sample");
            for (int column = 0; column < 7; column++) {
                double value = Double.parseDouble(columns[column].trim());
                if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite horizon sample");
                result[column][row - 1] = value;
            }
            double azimuth = result[0][row - 1];
            if (azimuth < 0 || azimuth >= 360 || (row > 1 && azimuth <= result[0][row - 2])) {
                throw new IllegalArgumentException("Horizon azimuths must be ordered in [0, 360)");
            }
        }
        return result;
    }
    /** Average the two adjacent skyline samples, wrapping across north. */
    static boolean isAbove(double[][] profile, Position position) {
        int index = findClosestAzimuthIndex(profile[0], position.azimuth);
        double[] heights = profile[2];
        int next = (index + 1) % heights.length;
        return position.height > (heights[index] + heights[next]) / 2;
    }

    private static int findClosestAzimuthIndex(double[] azimuths, double azimuth) {
        // Usa ricerca binaria per trovare l'indice
        int left = 0, right = azimuths.length - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (azimuths[mid] < azimuth) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return Math.max(0, left - 1);
    }

}
