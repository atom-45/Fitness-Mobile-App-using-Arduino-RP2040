package com.atom.bluetoothfitnessapplication.dsp;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class RangeOfMotionCalculator {

    private static final String TAG = "ROMCalculator";

    /**
     * Calculates Range of Motion (ROM) in degrees for each repetition.
     * Integrates over midpoint windows (peak[i] + peak[i+1]) / 2.
     * Gyroscope signal is directly in degrees per second (deg/s).
     *
     * @param gyroSignal       Raw or filtered 1D array of gyroscope values in deg/s.
     * @param repPeaks         List of peak sample indices detected for reps.
     * @param samplingRate     IMU sampling rate in Hz (e.g., 50.0f).
     * @param gyroInDegrees    Deprecated flag preserved for compatibility.
     * @return List of ROM values in degrees for each detected rep.
     */
    public static List<Float> calculateRangeOfMotion(
            float[] gyroSignal,
            List<Integer> repPeaks,
            float samplingRate,
            boolean gyroInDegrees
    ) {
        return calculateRangeOfMotion(gyroSignal, repPeaks, samplingRate);
    }

    public static List<Float> calculateRangeOfMotion(
            float[] gyroSignal,
            List<Integer> repPeaks,
            float samplingRate
    ) {
        List<Float> romResults = new ArrayList<>();
        if (repPeaks == null || repPeaks.size() < 2 || gyroSignal == null || gyroSignal.length < 2 || samplingRate <= 0) {
            Log.d(TAG, "Not enough peaks for ROM: " + (repPeaks != null ? repPeaks.size() : 0));
            return romResults;
        }

        float dt = 1.0f / samplingRate;

        for (int i = 0; i < repPeaks.size() - 1; i++) {
            int currentPeak = repPeaks.get(i);
            int nextPeak = repPeaks.get(i + 1);

            // Define window start/end boundaries at midpoints (peak[i] + peak[i+1]) / 2
            int startIdx;
            if (i == 0) {
                startIdx = Math.max(0, currentPeak - (nextPeak - currentPeak) / 2);
            } else {
                int prevPeak = repPeaks.get(i - 1);
                startIdx = (prevPeak + currentPeak) / 2;
            }
            int endIdx = (currentPeak + nextPeak) / 2;

            // Trapezoidal numerical integration over the window
            float angularDisplacementSum = 0.0f;
            for (int k = startIdx; k < endIdx - 1; k++) {
                float val1 = Math.abs(gyroSignal[k]);
                float val2 = Math.abs(gyroSignal[k + 1]);

                // Trapezoid area = (y1 + y2) / 2 * dt
                angularDisplacementSum += ((val1 + val2) / 2.0f) * dt;
            }

            // One-way ROM = Total back-and-forth rotation / 2
            float oneWayRomDegrees = angularDisplacementSum / 2.0f;
            romResults.add(oneWayRomDegrees);
            Log.d(TAG, "Rep " + i + " ROM: " + oneWayRomDegrees + " deg (Window: [" + startIdx + ", " + endIdx + "])");
        }

        return romResults;
    }
}
