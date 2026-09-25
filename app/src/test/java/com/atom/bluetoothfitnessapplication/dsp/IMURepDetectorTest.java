package com.atom.bluetoothfitnessapplication.dsp;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IMURepDetectorTest {

    @Test
    public void testFindPeaksDetectsDistinctPeaksAboveThreshold() {
        // Create synthetic signal with 3 distinct peaks of height 25.0
        float[] signal = new float[300];
        // Peak 1 at index 50
        signal[49] = 10.0f; signal[50] = 25.0f; signal[51] = 10.0f;
        // Peak 2 at index 150
        signal[149] = 10.0f; signal[150] = 28.0f; signal[151] = 10.0f;
        // Peak 3 at index 250
        signal[249] = 10.0f; signal[250] = 22.0f; signal[251] = 10.0f;

        float minHeight = 15.0f;
        int minDistance = 50;

        List<Integer> peaks = IMURepDetector.findPeaks(signal, minHeight, minDistance);

        assertEquals("Should detect exactly 3 peaks", 3, peaks.size());
        assertEquals(50, (int) peaks.get(0));
        assertEquals(150, (int) peaks.get(1));
        assertEquals(250, (int) peaks.get(2));
    }

    @Test
    public void testFindPeaksIgnoresSubThresholdNoise() {
        float[] signal = new float[100];
        // Noise peaks below threshold 15.0
        signal[20] = 10.0f;
        signal[50] = 12.5f;
        signal[80] = 8.0f;

        List<Integer> peaks = IMURepDetector.findPeaks(signal, 15.0f, 20);

        assertTrue("Should detect 0 peaks below threshold", peaks.isEmpty());
    }

    @Test
    public void testFindPeaksEnforcesMinimumDistance() {
        float[] signal = new float[100];
        // Two close peaks at index 30 and index 35 (distance 5 < minDistance 20)
        signal[29] = 10.0f; signal[30] = 20.0f; signal[31] = 10.0f;
        signal[34] = 10.0f; signal[35] = 30.0f; signal[36] = 10.0f; // Higher peak

        List<Integer> peaks = IMURepDetector.findPeaks(signal, 15.0f, 20);

        assertEquals("Should keep only 1 peak when within minDistance window", 1, peaks.size());
        assertEquals("Should select the higher amplitude peak at index 35", 35, (int) peaks.get(0));
    }
}
