
package com.atom.bluetoothfitnessapplication.utilities;

import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AnalyzerUtils {

    /**
     * Detects repetitions in acceleration data using peak detection.
     * Simple implementation looking for peaks above a threshold with a minimum distance.
     */
    public static int countReps(List<Float> magnitudes) {
        if (magnitudes.size() < 10) return 0;

        float threshold = 1.5f; // G's
        int minDistance = 10; // Samples between reps
        int count = 0;
        int lastPeakIndex = -minDistance;

        for (int i = 1; i < magnitudes.size() - 1; i++) {
            float val = magnitudes.get(i);
            if (val > threshold && val > magnitudes.get(i - 1) && val > magnitudes.get(i + 1)) {
                if (i - lastPeakIndex >= minDistance) {
                    count++;
                    lastPeakIndex = i;
                }
            }
        }
        return count;
    }

    public static float calculateMean(List<Float> data) {
        if (data.isEmpty()) return 0;
        float sum = 0;
        for (float f : data) sum += f;
        return sum / data.size();
    }

    public static float calculateMax(List<Float> data) {
        if (data.isEmpty()) return 0;
        return Collections.max(data);
    }

    public static float calculateStdDev(List<Float> data, float mean) {
        if (data.isEmpty()) return 0;
        float sum = 0;
        for (float f : data) {
            sum += Math.pow(f - mean, 2);
        }
        return (float) Math.sqrt(sum / data.size());
    }

    public static List<Float> calculateMagnitudes(List<Float> x, List<Float> y, List<Float> z) {
        List<Float> magnitudes = new ArrayList<>();
        for (int i = 0; i < x.size(); i++) {
            magnitudes.add((float) Math.sqrt(Math.pow(x.get(i), 2) + Math.pow(y.get(i), 2) + Math.pow(z.get(i), 2)));
        }
        return magnitudes;
    }

    /**
     * Stability Score (0-100%): Measures how steady the gyroscope is.
     * Higher variance = Lower stability.
     */
    public static float calculateStabilityScore(List<Float> gyroMagnitudes) {
        if (gyroMagnitudes.isEmpty()) return 0;
        float mean = calculateMean(gyroMagnitudes);
        float variance = 0;
        for (float f : gyroMagnitudes) variance += Math.pow(f - mean, 2);
        variance /= gyroMagnitudes.size();
        
        // Map variance to 0-100 score. 0.05 rad/s variance is very shaky.
        float score = 100f - (variance * 2000f); 
        return Math.max(0f, Math.min(100f, score));
    }

    /**
     * Symmetry Score (0-100%): Measures balance of movement.
     * Orientation Agnostic: Compares Net Drift vs Total Motion across all axes.
     */
    public static float calculateSymmetryScore(List<Float> gx, List<Float> gy, List<Float> gz) {
        if (gx.isEmpty() || gy.isEmpty() || gz.isEmpty()) return 0;

        float driftX = 0, driftY = 0, driftZ = 0;
        float totalAbsoluteMotion = 0;

        for (int i = 0; i < gx.size(); i++) {
            float x = gx.get(i);
            float y = gy.get(i);
            float z = gz.get(i);

            driftX += x;
            driftY += y;
            driftZ += z;
            
            totalAbsoluteMotion += (Math.abs(x) + Math.abs(y) + Math.abs(z));
        }

        if (totalAbsoluteMotion == 0) return 100f;

        // Sum the magnitude of drifts across all axes (prevents cross-axis cancellation)
        float totalAbsDrift = Math.abs(driftX) + Math.abs(driftY) + Math.abs(driftZ);
        
        float biasRatio = totalAbsDrift / totalAbsoluteMotion;
        
        // 0 bias = 100%, 0.5 bias (moving mostly one way) = 0%
        float score = (1.0f - (biasRatio * 2.0f)) * 100f; 
        
        return Math.max(0f, Math.min(100f, score));
    }

    /**
     * Returns a delta message comparing current value to the average of previous ones.
     */
    public static String getImprovementMessage(float current, List<WorkoutSummary> past, String metric) {
        if (past.isEmpty()) return "";
        
        float sum = 0;
        for (WorkoutSummary s : past) {
            switch (metric) {
                case "reps": sum += s.getRepCount(); break;
                case "power": sum += s.getMaxPower(); break;
                case "consistency": sum += s.getConsistency(); break;
                case "stability": sum += s.getStabilityScore(); break;
                case "symmetry": sum += s.getSymmetryScore(); break;
                case "duration": sum += s.getDuration(); break;
            }
        }
        float avg = sum / past.size();
        if (avg == 0) return "";
        float delta = ((current - avg) / avg) * 100;
        
        if (Math.abs(delta) < 1) return "No change";
        return String.format(Locale.ENGLISH,"%s %.1f%% vs last %d", delta > 0 ? "↑" : "↓", Math.abs(delta), past.size());
    }
}
