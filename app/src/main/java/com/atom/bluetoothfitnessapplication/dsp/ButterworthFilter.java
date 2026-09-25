package com.atom.bluetoothfitnessapplication.dsp;

import android.util.Log;

/**
 * Low-pass zero-phase Butterworth filter matching Python SciPy's scipy.signal.filtfilt
 * and scipy.signal.butter behavior.
 */
public class ButterworthFilter {

    private static final String TAG = "ButterworthFilter";

    /**
     * Applies a low-pass zero-phase Butterworth filter (equivalent to scipy.signal.filtfilt).
     *
     * In SciPy, passing a 2nd-order base filter through scipy.signal.filtfilt (forward + backward)
     * yields an effective zero-phase 4th-order response with exact -3dB cutoff at cutoffFreq.
     *
     * @param data         Raw input signal array
     * @param cutoffFreq   Cutoff frequency in Hz (e.g., 2.5)
     * @param samplingRate Sampling frequency in Hz (e.g., 50.0)
     * @return Cleaned, filtered signal array of the same length
     */
    public static float[] filterSignal(float[] data, float cutoffFreq, float samplingRate) {
        if (data == null || data.length == 0) return data;

        // 1. Calculate 2nd-order low-pass Butterworth coefficients (b and a)
        // Equivalent to scipy.signal.butter(N=2, Wn=cutoffFreq/(samplingRate/2), btype='low')
        double[][] coeffs = computeButterworth2ndOrderCoefficients(cutoffFreq, samplingRate);
        double[] b = coeffs[0];
        double[] a = coeffs[1];

        // 2. Forward pass with steady-state initial conditions (scipy lfilter_zi)
        double[] forward = filterOneDirectionWithZi(data, b, a);

        // 3. Reverse array
        double[] reversed = reverseArray(forward);

        // 4. Backward pass (zero-phase filtering) with steady-state initial conditions
        double[] backward = filterOneDirectionWithZi(reversed, b, a);

        // 5. Reverse back to original chronological order
        double[] resultDouble = reverseArray(backward);

        // Cast back to float array and compute summary ranges
        float[] result = new float[data.length];
        float maxRaw = -Float.MAX_VALUE, minRaw = Float.MAX_VALUE;
        float maxFilt = -Float.MAX_VALUE, minFilt = Float.MAX_VALUE;

        for (int i = 0; i < data.length; i++) {
            result[i] = (float) resultDouble[i];
            if (data[i] > maxRaw) maxRaw = data[i];
            if (data[i] < minRaw) minRaw = data[i];
            if (result[i] > maxFilt) maxFilt = result[i];
            if (result[i] < minFilt) minFilt = result[i];
        }

        Log.d(TAG, "Filter (SciPy-filtfilt): RawRange[" + minRaw + ", " + maxRaw + "] -> FiltRange[" + minFilt + ", " + maxFilt + "] samples=" + data.length);

        return result;
    }

    /**
     * Computes b (numerator) and a (denominator) coefficients for a 2nd-order low-pass Butterworth filter.
     * Equivalent to scipy.signal.butter(N=2, Wn=cutoffFreq/(samplingRate/2), btype='low')
     */
    public static double[][] computeButterworth2ndOrderCoefficients(double cutoffFreq, double samplingRate) {
        // Ensure cutoff is strictly below Nyquist frequency
        double nyquist = samplingRate / 2.0;
        double safeCutoff = Math.min(cutoffFreq, nyquist * 0.99);

        // Pre-warped frequency K = tan(pi * cutoff / fs)
        double K = Math.tan(Math.PI * safeCutoff / samplingRate);
        double K2 = K * K;
        double norm = 1.0 + Math.sqrt(2.0) * K + K2;

        double b0 = K2 / norm;
        double b1 = 2.0 * b0;
        double b2 = b0;

        double a0 = 1.0;
        double a1 = 2.0 * (K2 - 1.0) / norm;
        double a2 = (1.0 - Math.sqrt(2.0) * K + K2) / norm;

        return new double[][]{
                {b0, b1, b2},
                {a0, a1, a2}
        };
    }

    /**
     * Applies Direct Form II Transposed IIR filtering with steady-state initial conditions (zi).
     * Prevents edge transient drops at signal start.
     */
    private static double[] filterOneDirectionWithZi(float[] input, double[] b, double[] a) {
        int n = input.length;
        double[] output = new double[n];

        if (n == 0) return output;

        // Compute steady-state initial state zi for x[0]
        double x0 = input[0];
        double d0 = (b[1] - a[1] + b[2] - a[2]) * x0;
        double d1 = (b[2] - a[2]) * x0;

        for (int i = 0; i < n; i++) {
            double x = input[i];
            double y = b[0] * x + d0;

            d0 = b[1] * x - a[1] * y + d1;
            d1 = b[2] * x - a[2] * y;

            output[i] = y;
        }

        return output;
    }

    private static double[] filterOneDirectionWithZi(double[] input, double[] b, double[] a) {
        int n = input.length;
        double[] output = new double[n];

        if (n == 0) return output;

        // Compute steady-state initial state zi for x[0]
        double x0 = input[0];
        double d0 = (b[1] - a[1] + b[2] - a[2]) * x0;
        double d1 = (b[2] - a[2]) * x0;

        for (int i = 0; i < n; i++) {
            double x = input[i];
            double y = b[0] * x + d0;

            d0 = b[1] * x - a[1] * y + d1;
            d1 = b[2] * x - a[2] * y;

            output[i] = y;
        }

        return output;
    }

    private static double[] reverseArray(double[] array) {
        double[] reversed = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            reversed[i] = array[array.length - 1 - i];
        }
        return reversed;
    }
}
