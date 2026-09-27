package it.howsthere.howsthere2;

import static org.junit.Assert.*;
import org.junit.Test;

public class HorizonDataTest {
    @Test public void comparesSkylineAcrossNorthAndAtSampleBoundaries() {
        double[][] profile = HorizonData.parseProfile(HorizonRequestTest.profile());
        profile[2][359] = 20;
        profile[2][0] = 10;
        assertTrue(HorizonData.isAbove(profile, position(359.5, 16)));
        assertFalse(HorizonData.isAbove(profile, position(359.5, 15)));
        profile[2][10] = 30;
        profile[2][11] = 10;
        assertTrue(HorizonData.isAbove(profile, position(10.5, 21)));
        assertFalse(HorizonData.isAbove(profile, position(10.5, 20)));
        // An exact azimuth uses the preceding interval, as in the original calculation.
        assertFalse(HorizonData.isAbove(profile, position(11, 20)));
        assertTrue(HorizonData.isAbove(profile, position(0, 11)));
    }

    private static it.howsthere.howsthere2.objects.Position position(double azimuth, double height) {
        return new it.howsthere.howsthere2.objects.Position(12, 0, height, azimuth);
    }

    @Test public void parsesAllSamplesAndColumns() {
        double[][] values = HorizonData.parseProfile(HorizonRequestTest.profile());
        assertEquals(7, values.length);
        assertEquals(360, values[0].length);
        assertEquals(359, values[0][359], 0);
        assertEquals(2000, values[6][0], 0);
    }
    @Test public void rejectsIncompleteData() {
        assertThrows(IllegalArgumentException.class, () -> HorizonData.parseProfile("header\n0,1,2"));
    }
    @Test public void rejectsNonFiniteData() {
        assertThrows(IllegalArgumentException.class, () -> HorizonData.parseProfile(
                HorizonRequestTest.profile().replace(",2000", ",NaN")));
    }
    @Test public void rejectsUnorderedAzimuths() {
        assertThrows(IllegalArgumentException.class, () -> HorizonData.parseProfile(
                HorizonRequestTest.profile().replace("359,0,10", "0,0,10")));
    }
}
