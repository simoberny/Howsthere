package it.howsthere.howsthere2.ui.result;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArProjectionTest {
    // Phone held upright, rear camera facing north; device Y points up.
    private static final float[] NORTH = {1, 0, 0, 0, 0, -1, 0, 1, 0};

    private float[] project(double azimuth, double altitude, int sensor) {
        float[] point = new float[2];
        assertTrue(ArProjection.project(azimuth, altitude, NORTH, 0, 0, 0,
                sensor, 1000, 1000, 2000, 1500, 1, point));
        return point;
    }

    @Test public void projectsForwardRayAtOpticalCenter() {
        for (int rotation : new int[]{0, 90, 180, 270}) {
            assertArrayEquals(new float[]{2000, 1500}, project(0, 0, rotation), 0.01f);
        }
    }

    @Test public void respectsSensorRotationForHorizontalAndVerticalRays() {
        assertEquals(3000, project(45, 0, 0)[0], 0.01);
        assertEquals(500, project(0, 45, 0)[1], 0.01);
        // A 90-degree sensor is rotated clockwise by the preview. Right maps to raw -Y.
        assertEquals(500, project(45, 0, 90)[1], 0.01);
        assertEquals(1000, project(0, 45, 90)[0], 0.01);
        assertEquals(2500, project(45, 0, 270)[1], 0.01);
    }

    @Test public void crossesNorthWithoutWrappingAcrossTheScreen() {
        assertTrue(project(359, 0, 0)[0] < 2000);
        assertTrue(project(1, 0, 0)[0] > 2000);
        assertArrayEquals(project(0, 0, 0), project(360, 0, 0), 0.01f);
    }

    @Test public void rejectsRaysBehindCameraAndAtProjectionSingularity() {
        float[] point = new float[2];
        for (int azimuth : new int[]{90, 180, 270}) {
            assertFalse(ArProjection.project(azimuth, 0, NORTH, 0, 0, 0,
                    90, 1000, 1000, 2000, 1500, 1, point));
        }
    }

    @Test public void appliesMagneticDeclinationAndManualAlignment() {
        float[] point = new float[2];
        assertTrue(ArProjection.project(12, -3, NORTH, 10, -2, 3,
                90, 1000, 1000, 2000, 1500, 1, point));
        assertArrayEquals(new float[]{2000, 1500}, point, 0.01f);
        assertEquals(10, ArProjection.bearing(NORTH, 10), 0.001);
    }

    @Test public void handlesRollWithoutChangingTheCentralRay() {
        float[] rolled = {0, -1, 0, 0, 0, -1, 1, 0, 0};
        float[] point = new float[2];
        assertTrue(ArProjection.project(0, 0, rolled, 0, 0, 0,
                90, 1000, 1000, 2000, 1500, 1, point));
        assertArrayEquals(new float[]{2000, 1500}, point, 0.01f);
        assertTrue(ArProjection.project(45, 0, rolled, 0, 0, 0,
                90, 1000, 1000, 2000, 1500, 1, point));
        assertEquals(3000, point[0], 0.01);
    }
}
