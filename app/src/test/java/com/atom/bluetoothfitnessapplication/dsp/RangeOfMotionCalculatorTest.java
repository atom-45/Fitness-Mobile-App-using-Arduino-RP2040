package com.atom.bluetoothfitnessapplication.dsp;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RangeOfMotionCalculatorTest {

    @Test
    public void testCalculateRangeOfMotionTrapezoidalIntegration() {
        // Gyro signal in deg/s
        float[] gyroSignal = new float[100];
        // Create a half-sine angular velocity profile between index 20 and index 80
        float maxDegPerSec = (float) (2.0 * (180.0 / Math.PI));
        for (int i = 20; i <= 80; i++) {
            gyroSignal[i] = (float) (maxDegPerSec * Math.sin(Math.PI * (i - 20) / 60.0));
        }

        List<Integer> repPeaks = Arrays.asList(30, 70);
        float sampleRate = 50.0f; // 50 Hz

        List<Float> roms = RangeOfMotionCalculator.calculateRangeOfMotion(gyroSignal, repPeaks, sampleRate, true);

        assertFalse("ROM results should not be empty", roms.isEmpty());
        float romDegrees = roms.get(0);
        assertEquals(20.73f, romDegrees, 1.0f);
    }
}
