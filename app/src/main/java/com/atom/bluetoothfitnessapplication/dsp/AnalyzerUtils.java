package com.atom.bluetoothfitnessapplication.dsp;

import android.util.Log;

import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;
import com.atom.bluetoothfitnessapplication.utilities.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AnalyzerUtils {

    private static final String TAG = "AnalyzerUtils";

    /**
     * Calculates the sample rate (Hz) for an exercise session based on total samples and session duration.
     * @param totalSamples Number of data samples collected during the session.
     * @param durationInSeconds Duration of the exercise session in seconds.
     * @return Sample rate in Hz, or a default fallback (50.0f Hz) if inputs are invalid.
     */
    public static float calculateSampleRate(int totalSamples, float durationInSeconds) {
        if (totalSamples <= 0 || durationInSeconds <= 0) {
            return 50.0f; // Default fallback sample rate in Hz
        }
        return (float) totalSamples / durationInSeconds;
    }

    /**
     * Helper overload to calculate sample rate from a data list and session duration in seconds.
     */
    public static float calculateSampleRate(List<?> dataList, float durationInSeconds) {
        int length = (dataList != null) ? dataList.size() : 0;
        return calculateSampleRate(length, durationInSeconds);
    }

    /**
     * Calculates exercise stats (Reps, ROM) depending on the exercise type and inputs.
     * - Sit-Ups: Uses GyroX and AccY with min distance 180 (filtered GyroX for peaks & reps).
     * - Push-Ups: Uses Acceleration Z (AccZ) with min distance 130 to find peaks & reps.
     */
    public static Pair<Integer, Float> calculateExerciseStats(
            String exercise,
            List<Float> accData,
            List<Float> gyroData,
            float sampleRate
    ) {
        if (accData == null || accData.isEmpty()) {
            return new Pair<>(0, 0f);
        }

        float samplingRate = sampleRate > 0 ? sampleRate : 71.61f;
        String ex = exercise != null ? exercise.trim() : "";

        if (ex.contains("sit")) {
            if (gyroData == null || gyroData.size() < 20) {
                return new Pair<>(0, 0f);
            }
            float cutoffFreq = 2.5f;

            // 1. Convert AccY and GyroX data to arrays
            float[] accYArr = new float[accData.size()];
            for (int i = 0; i < accData.size(); i++) {
                accYArr[i] = accData.get(i);
            }
            float[] gyroXArr = new float[gyroData.size()];
            for (int i = 0; i < gyroData.size(); i++) {
                gyroXArr[i] = gyroData.get(i);
            }

            // 2. Filter the signals
            float[] filteredAccY = ButterworthFilter.filterSignal(accYArr, cutoffFreq, samplingRate);
            float[] filteredGyroX = ButterworthFilter.filterSignal(gyroXArr, cutoffFreq, samplingRate);

            // 3. Detect peaks on AccY and GyroX with minDistance = 180
            List<Integer> accYPeaks = IMURepDetector.findPeaks(filteredAccY, 0.5f, 180);
            List<Integer> gyroPeaks = IMURepDetector.findPeaks(filteredGyroX, 15.0f, 180);

            List<Integer> repPeaks = !gyroPeaks.isEmpty() ? gyroPeaks : accYPeaks;
            int repCount = repPeaks.size();
            Log.d(TAG, "Sit-Up Counting: Reps=" + repCount + " (AccYPeaks=" + accYPeaks.size() + ", GyroPeaks=" + gyroPeaks.size() + ", minDistance=180)");

            // 4. Calculate Range of Motion using detected peaks (gyro data in deg/s)
            List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                    filteredGyroX,
                    repPeaks,
                    samplingRate,
                    true
            );

            float avgRom = 0f;
            if (!romValues.isEmpty()) {
                float sum = 0f;
                for (float r : romValues) sum += r;
                avgRom = sum / romValues.size();
            } else if (repCount > 0) {
                List<Float> filteredGyroXAbs = new ArrayList<>();
                for (float f : filteredGyroX) filteredGyroXAbs.add(Math.abs(f));
                avgRom = calculateRangeOfMotion(filteredGyroXAbs, repCount, samplingRate);
            }
            return new Pair<>(repCount, avgRom);

        } else if (ex.contains("push")) {
            if (accData.size() < 20) {
                return new Pair<>(0, 0f);
            }
            float cutoffFreq = 2.0f;

            // 1. Convert AccZ data to array
            float[] accZArr = new float[accData.size()];
            for (int i = 0; i < accData.size(); i++) {
                accZArr[i] = accData.get(i);
            }

            // 2. Filter Acceleration Z and GyroX
            float[] filteredAccZ = ButterworthFilter.filterSignal(accZArr, cutoffFreq, samplingRate);

            float[] filteredGyroX = null;
            List<Integer> gyroXPeaks = new ArrayList<>();
            if (gyroData != null && gyroData.size() >= 20) {
                float[] gyroXArr = new float[gyroData.size()];
                for (int i = 0; i < gyroData.size(); i++) {
                    gyroXArr[i] = gyroData.get(i);
                }
                filteredGyroX = ButterworthFilter.filterSignal(gyroXArr, 2.5f, samplingRate);
                gyroXPeaks = IMURepDetector.findPeaks(filteredGyroX, 15.0f, 130);
            }

            // 3. Detect peaks on AccZ with minDistance = 130
            List<Integer> accZPeaks = IMURepDetector.findPeaks(filteredAccZ, 0.5f, 130);

            List<Integer> repPeaks = !gyroXPeaks.isEmpty() ? gyroXPeaks : accZPeaks;
            int repCount = repPeaks.size();
            Log.d(TAG, "Push-Up Counting: Reps=" + repCount + " (AccZPeaks=" + accZPeaks.size() + ", GyroPeaks=" + gyroXPeaks.size() + ", minDistance=130)");

            // 4. Calculate Range of Motion using Gyroscope X
            float avgRom = 0f;
            if (filteredGyroX != null && !repPeaks.isEmpty()) {
                List<Float> romValues = RangeOfMotionCalculator.calculateRangeOfMotion(
                        filteredGyroX,
                        repPeaks,
                        samplingRate,
                        true
                );

                if (!romValues.isEmpty()) {
                    float sum = 0f;
                    for (float r : romValues) sum += r;
                    avgRom = sum / romValues.size();
                } else if (repCount > 0) {
                    List<Float> filteredGyroXAbs = new ArrayList<>();
                    for (float f : filteredGyroX) filteredGyroXAbs.add(Math.abs(f));
                    avgRom = calculateRangeOfMotion(filteredGyroXAbs, repCount, samplingRate);
                }
            }

            return new Pair<>(repCount, avgRom);
        }

        // Default fallback
        int reps = countReps(accData, gyroData);
        float rom = (gyroData != null) ? calculateRangeOfMotion(gyroData, reps, samplingRate) : 0f;
        return new Pair<>(reps, rom);
    }

    public static Pair<Integer, Float> calculateSitUpStats(List<Float> accY, List<Float> gyroX, float sampleRate) {
        return calculateExerciseStats("sit up", accY, gyroX, sampleRate);
    }

    public static Pair<Integer, Float> calculateSitUpStats(List<Float> accY, List<Float> gyroX) {
        return calculateSitUpStats(accY, gyroX, 71.61f);
    }

    /**
     * Multi-Modal Rep Counting: Handshake between Accelerometer and Gyroscope.
     * Uses a low 1.1G threshold cross-validated with rotational motion.
     */
    public static int countReps(List<Float> accMagnitudes, List<Float> gyroMagnitudes)
    {
        if (accMagnitudes.size() < 15 || gyroMagnitudes.size() < 15) return 0;

        float accThreshold = 1.1f;    // Lowered threshold for sensitivity
        float gyroThreshold = 0.4f;   // Threshold to detect actual body rotation
        
        int count = 0;
        int lastRepIndex = -20; 
        boolean motionDetected = false;

        for (int i = 1; i < accMagnitudes.size() - 1; i++)
        {
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

    public static float calculateMean(List<Float> data)
    {
        if (data.isEmpty()) return 0;
        float sum = 0;
        for (float f : data) sum += f;
        return sum / data.size();
    }

    public static float calculateMax(List<Float> data)
    {
        if (data.isEmpty()) return 0;
        return Collections.max(data);
    }

    public static float calculateStdDev(List<Float> data, float mean)
    {
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
    public static float calculateStabilityScore(List<Float> gyroMagnitudes)
    {
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
    public static float calculateSymmetryScore(List<Float> gx, List<Float> gy, List<Float> gz)
    {
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
     * Estimates Range of Motion in degrees using session sample rate.
     * Integrates average single-stroke ROM from deg/s input.
     */
    public static float calculateRangeOfMotion(List<Float> gyroMagnitudes, int repCount, float sampleRate) {
        if (gyroMagnitudes == null || gyroMagnitudes.isEmpty() || repCount <= 0 || sampleRate <= 0) return 0f;

        float dt = 1.0f / sampleRate;
        float totalRotationDegPerSec = 0f;
        int activeSamples = 0;

        for (float f : gyroMagnitudes) {
            totalRotationDegPerSec += f;
            activeSamples++;
        }

        if (activeSamples == 0) return 0f;

        // Total degrees integrated over movement (gyro is already in deg/s)
        float totalDegrees = totalRotationDegPerSec * dt;

        // Average single-stroke ROM = total degrees / (reps * 2)
        return totalDegrees / (repCount * 2);
    }

    public static float calculateRangeOfMotion(List<Float> gyroMagnitudes, int repCount) {
        return calculateRangeOfMotion(gyroMagnitudes, repCount, 10.0f);
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
