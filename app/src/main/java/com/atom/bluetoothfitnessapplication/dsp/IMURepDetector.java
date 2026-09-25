package com.atom.bluetoothfitnessapplication.dsp;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class IMURepDetector {

    private static final String TAG = "IMURepDetector";

    /**
     * Extracts positive peak indices from a signal (e.g., gyro_x).
     */
    public static List<Integer> findPeaks(float[] signal, float minHeight, int minDistance) {
        List<Integer> candidatePeaks = new ArrayList<>();

        // 1. Identify local maxima above minHeight
        for (int i = 1; i < signal.length - 1; i++) {
            if (signal[i] > signal[i - 1] && signal[i] > signal[i + 1]) {
                if (signal[i] >= minHeight) {
                    candidatePeaks.add(i);
                }
            }
        }

        List<Integer> finalPeaks = filterByMinDistance(signal, candidatePeaks, minDistance);
        Log.d(TAG, "findPeaks: Candidates=" + candidatePeaks.size() + ", Final=" + finalPeaks.size() + " (minHeight=" + minHeight + ")");
        return finalPeaks;
    }

    /**
     * Extracts trough indices by inverting the signal first (e.g., acceleration_y).
     * Converts downward dips into positive peaks to avoid double-counting split crests.
     */
    public static List<Integer> findTroughs(float[] signal, float troughDepthThreshold, int minDistance) {
        float[] invertedSignal = new float[signal.length];

        // Invert signal: -acceleration_y
        for (int i = 0; i < signal.length; i++) {
            invertedSignal[i] = -signal[i];
        }

        // Troughs at -1.1 become positive peaks at +1.1
        List<Integer> candidateTroughs = new ArrayList<>();
        for (int i = 1; i < invertedSignal.length - 1; i++) {
            if (invertedSignal[i] > invertedSignal[i - 1] && invertedSignal[i] > invertedSignal[i + 1]) {
                if (invertedSignal[i] >= troughDepthThreshold) {
                    candidateTroughs.add(i);
                }
            }
        }

        List<Integer> finalTroughs = filterByMinDistance(invertedSignal, candidateTroughs, minDistance);
        Log.d(TAG, "findTroughs: Candidates=" + candidateTroughs.size() + ", Final=" + finalTroughs.size() + " (depthThreshold=" + troughDepthThreshold + ")");
        return finalTroughs;
    }

    /**
     * Enforces minDistance by keeping the highest peak within any conflicting window.
     */
    private static List<Integer> filterByMinDistance(float[] signal, List<Integer> peaks, int minDistance) {
        if (peaks.isEmpty()) return new ArrayList<>();

        List<Integer> filteredPeaks = new ArrayList<>();
        int currentPeak = peaks.get(0);

        for (int i = 1; i < peaks.size(); i++) {
            int nextPeak = peaks.get(i);

            if (nextPeak - currentPeak >= minDistance) {
                filteredPeaks.add(currentPeak);
                currentPeak = nextPeak;
            } else {
                // If peaks are too close, keep the one with the higher amplitude
                if (signal[nextPeak] > signal[currentPeak]) {
                    currentPeak = nextPeak;
                }
            }
        }
        filteredPeaks.add(currentPeak);

        return filteredPeaks;
    }
}
