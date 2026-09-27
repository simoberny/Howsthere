package it.howsthere.howsthere2.ui.result;

import static org.junit.Assert.*;
import org.junit.Test;

public class PeakLabelSpaceTest {
    @Test public void keepsGapAbovePeakEvenAtTopEdge() {
        assertEquals(-8f, PeakLabelSpace.bottom(2, new float[0], 0, 20, 10), 0);
    }
    @Test public void avoidsHigherNeighbourUnderLabelWidth() {
        float[] terrain = {0, 100, 10, 30, 20, 100};
        assertEquals(20f, PeakLabelSpace.bottom(100, terrain, 8, 18, 10), 0);
    }
    @Test public void ignoresDistantHigherMountains() {
        float[] terrain = {0, 1, 10, 1, 100, 90, 120, 100};
        assertEquals(80f, PeakLabelSpace.bottom(100, terrain, 105, 115, 10), 0);
    }
}
