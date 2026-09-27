package it.howsthere.howsthere2.ui.result;

/** Pinhole projection from true azimuth/elevation to back-camera sensor pixels. */
final class ArProjection {
    private ArProjection() { }

    static boolean project(double azimuth, double altitude, float[] deviceToWorld,
                           double declination, double headingOffset, double altitudeOffset,
                           int sensorOrientation, double fx, double fy, double cx, double cy,
                           double scale, float[] out) {
        double bearing = Math.toRadians(azimuth + headingOffset - declination);
        double elevation = Math.toRadians(altitude + altitudeOffset);
        double east = Math.cos(elevation) * Math.sin(bearing);
        double north = Math.cos(elevation) * Math.cos(bearing);
        double up = Math.sin(elevation);
        // Android's rotation matrix maps device axes to magnetic east/north/up.
        // Its transpose maps world rays back to the device; the rear camera looks along -Z.
        double x = deviceToWorld[0] * east + deviceToWorld[3] * north + deviceToWorld[6] * up;
        double y = deviceToWorld[1] * east + deviceToWorld[4] * north + deviceToWorld[7] * up;
        double depth = -(deviceToWorld[2] * east + deviceToWorld[5] * north + deviceToWorld[8] * up);
        if (depth <= 0.05) return false;
        double rotation = Math.toRadians(sensorOrientation);
        double imageX = Math.cos(rotation) * x - Math.sin(rotation) * y;
        double imageY = -Math.sin(rotation) * x - Math.cos(rotation) * y;
        out[0] = (float) (cx + scale * fx * imageX / depth);
        out[1] = (float) (cy + scale * fy * imageY / depth);
        return Float.isFinite(out[0]) && Float.isFinite(out[1]);
    }

    static double bearing(float[] deviceToWorld, double declination) {
        return (Math.toDegrees(Math.atan2(-deviceToWorld[2], -deviceToWorld[5]))
                + declination + 360) % 360;
    }
}
