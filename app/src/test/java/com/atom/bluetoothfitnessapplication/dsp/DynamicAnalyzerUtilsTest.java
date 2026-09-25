package com.atom.bluetoothfitnessapplication.dsp;

import com.atom.bluetoothfitnessapplication.utilities.Pair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DynamicAnalyzerUtilsTest {

    @Test
    public void testCalculateSampleRate() {
        float sampleRate = DynamicAnalyzerUtils.calculateSampleRate(1000, 20.0f);
        assertEquals(50.0f, sampleRate, 0.001f);
    }

    @Test
    public void testCalculateDynamicMinSampleDistanceForSitUpAndPushUp() {
        float sampleRate = 200.0f; // 200 Hz BLE sampling rate

        // Sit-Up min distance (~1.2s min duration at 200 Hz with safety factor 0.75)
        int sitUpMinDist = DynamicAnalyzerUtils.calculateDynamicMinSampleDistance(sampleRate, 1.2f, 0.75f);
        assertEquals(180, sitUpMinDist);

        // Push-Up min distance (~0.8s min duration at 200 Hz with safety factor 0.81)
        int pushUpMinDist = DynamicAnalyzerUtils.calculateDynamicMinSampleDistance(sampleRate, 0.8f, 0.81f);
        assertEquals(130, pushUpMinDist);
    }

    @Test
    public void testCalculateAdaptiveMinDistance() {
        // Peaks at samples 100, 300, 500, 700 (avg interval = 200 samples)
        List<Integer> peaks = Arrays.asList(100, 300, 500, 700);
        int adaptiveDist = DynamicAnalyzerUtils.calculateAdaptiveMinDistance(peaks, 130);

        // Expected: 70% of 200 = 140
        assertEquals(140, adaptiveDist);
    }

    @Test
    public void testCalculateExerciseStatsSitUp() {
        List<Float> accY = new ArrayList<>();
        List<Float> gyroX = new ArrayList<>();

        for (int i = 0; i < 600; i++) {
            accY.add((float) Math.sin(i * 0.05));
            gyroX.add((float) (Math.cos(i * 0.05) * 2.0)); // rad/s
        }

        Pair<Integer, Float> stats = DynamicAnalyzerUtils.calculateExerciseStats(
                "Sit Up",
                accY,
                gyroX,
                50.0f
        );

        assertNotNull(stats);
        assertTrue("Reps should be non-negative", stats.first() >= 0);
        assertTrue("ROM should be non-negative", stats.second() >= 0.0f);
    }

    @Test
    public void testCalculateExerciseStatsPushUp() {
        List<Float> accZ = new ArrayList<>();
        List<Float> gyroX = new ArrayList<>();

        for (int i = 0; i < 600; i++) {
            accZ.add((float) Math.sin(i * 0.05));
            gyroX.add((float) (Math.cos(i * 0.05) * 2.0)); // rad/s
        }

        Pair<Integer, Float> stats = DynamicAnalyzerUtils.calculateExerciseStats(
                "Push Up",
                accZ,
                gyroX,
                50.0f
        );

        assertNotNull(stats);
        assertTrue("Reps should be non-negative", stats.first() >= 0);
        assertTrue("ROM should be non-negative", stats.second() >= 0.0f);
    }
}
