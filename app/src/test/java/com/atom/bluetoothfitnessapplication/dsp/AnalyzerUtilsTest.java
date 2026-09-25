package com.atom.bluetoothfitnessapplication.dsp;

import com.atom.bluetoothfitnessapplication.utilities.Pair;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AnalyzerUtilsTest {

    @Test
    public void testCalculateSampleRateWithValidData() {
        int totalSamples = 300;
        float durationSeconds = 10.0f;
        float sampleRate = DynamicAnalyzerUtils.calculateSampleRate(totalSamples, durationSeconds);
        assertEquals(30.0f, sampleRate, 0.001f);
    }

    @Test
    public void testCalculateSampleRateFromList() {
        List<Float> sampleData = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            sampleData.add((float) i);
        }
        float durationSeconds = 10.0f;
        float sampleRate = DynamicAnalyzerUtils.calculateSampleRate(sampleData, durationSeconds);
        assertEquals(50.0f, sampleRate, 0.001f);
    }

    @Test
    public void testCalculateSampleRateFallbackForInvalidInputs() {
        float fallbackRate1 = DynamicAnalyzerUtils.calculateSampleRate(0, 10.0f);
        assertEquals(50.0f, fallbackRate1, 0.001f);

        float fallbackRate2 = DynamicAnalyzerUtils.calculateSampleRate(100, 0f);
        assertEquals(50.0f, fallbackRate2, 0.001f);
    }

    @Test
    public void testCalculateSitUpStatsWithDynamicSampleRate() {
        List<Float> accY = new ArrayList<>();
        List<Float> gyroX = new ArrayList<>();

        // Generate synthetic data
        for (int i = 0; i < 100; i++) {
            accY.add((float) Math.sin(i * 0.1));
            gyroX.add((float) Math.cos(i * 0.1) * 20.0f);
        }

        float durationSeconds = 2.0f;
        float sampleRate = DynamicAnalyzerUtils.calculateSampleRate(accY, durationSeconds);
        assertEquals(50.0f, sampleRate, 0.001f);

        Pair<Integer, Float> stats = DynamicAnalyzerUtils.calculateExerciseStats("Sit Up", accY, gyroX, sampleRate);
        assertNotNull(stats);
    }

    @Test
    public void testCalculateRangeOfMotionWithDynamicSampleRate() {
        // Gyro magnitudes in deg/s
        List<Float> gyroMagnitudes = Arrays.asList(20.0f, 50.0f, 40.0f, 10.0f, 30.0f);
        int repCount = 2;
        float sampleRate = 50.0f;

        float rom = DynamicAnalyzerUtils.calculateRangeOfMotion(gyroMagnitudes, repCount, sampleRate);
        // Verify ROM is calculated with dynamic dt = 1/50 = 0.02s
        float expectedDt = 1.0f / 50.0f;
        float totalRotation = 20.0f + 50.0f + 40.0f + 10.0f + 30.0f;
        float expectedDegrees = totalRotation * expectedDt;
        float expectedRom = expectedDegrees / (repCount * 2);

        assertEquals(expectedRom, rom, 0.01f);
    }
}
