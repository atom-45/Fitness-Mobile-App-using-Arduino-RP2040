
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
    /**
     * Multi-Modal Rep Counting: Handshake between Accelerometer and Gyroscope.
     * Uses a low 1.1G threshold cross-validated with rotational motion.
     */
    public static int countReps(List<Float> accMagnitudes, List<Float> gyroMagnitudes) {
        if (accMagnitudes.size() < 15 || gyroMagnitudes.size() < 15) return 0;

        float accThreshold = 1.1f;    // Lowered threshold for sensitivity
        float gyroThreshold = 0.4f;   // Threshold to detect actual body rotation
        
        int count = 0;
        int lastRepIndex = -20; 
        boolean motionDetected = false;

        for (int i = 1; i < accMagnitudes.size() - 1; i++) {
            float aVal = accMagnitudes.get(i);
            float gVal = gyroMagnitudes.get(i);

            // Step 1: Detect that the body is in a state of rotation (The Arc)
            if (gVal > gyroThreshold) {
                motionDetected = true;
            }

            // Step 2: Look for the turnaround point (The Peak) while motion is active
            if (motionDetected) {
                if (aVal > accThreshold && aVal > accMagnitudes.get(i - 1) && aVal > accMagnitudes.get(i + 1)) {
                    // Step 3: Verify timing (Debounce)
                    if (i - lastRepIndex > 15) {
                        count++;
                        lastRepIndex = i;
                        motionDetected = false; // Reset for next arc
                    }
                }
            }

            // Reset motion detection if rotation stops without a peak (noise filter)
            if (gVal < 0.1f) {
                motionDetected = false;
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
     * Stability Score (0-100%): Measures how steady the body is held.
     * Uses an exponential decay function to be forgiving of micro-vibrations
     * while penalizing significant wobbles.
     */
    public static float calculateStabilityScore(List<Float> gyroMagnitudes) {
        if (gyroMagnitudes.isEmpty()) return 0;
        
        float mean = calculateMean(gyroMagnitudes);
        float variance = 0;
        for (float f : gyroMagnitudes) variance += Math.pow(f - mean, 2);
        variance /= gyroMagnitudes.size();
        
        // Exponential mapping:
        // Score = 100 * e^(-k * variance)
        // k constant (8.0) ensures 0.2 variance is ~20% and 0.05 is ~67%
        double k = 8.0; 
        float score = (float) (Math.exp(-k * variance) * 100f);
        
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
     * Estimates Range of Motion in degrees.
     * Assumes an average sample rate of 10Hz (0.1s per sample).
     */
    public static float calculateRangeOfMotion(List<Float> gyroMagnitudes, int repCount) {
        if (gyroMagnitudes.isEmpty() || repCount == 0) return 0;

        float totalRotation = 0;
        for (float f : gyroMagnitudes) totalRotation += f;

        // Total rotation is in radians/sec. Convert to degrees.
        // degrees = (sum of rad/s) * dt * (180/PI)
        float dt = 0.1f; // 100ms sample interval
        float totalDegrees = totalRotation * dt * (180f / (float) Math.PI);

        // One rep is down and up. We want the single stroke degrees (e.g. 0 to 90).
        return totalDegrees / (repCount * 2);
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
                case "rom": sum += s.getRangeOfMotion(); break;
            }
        }
        float avg = sum / past.size();
        if (avg == 0) return "";
        float delta = ((current - avg) / avg) * 100;
        
        if (Math.abs(delta) < 1) return "No change";
        return String.format(Locale.ENGLISH,"%s %.1f%% vs last %d", delta > 0 ? "↑" : "↓", Math.abs(delta), past.size());
    }
}
