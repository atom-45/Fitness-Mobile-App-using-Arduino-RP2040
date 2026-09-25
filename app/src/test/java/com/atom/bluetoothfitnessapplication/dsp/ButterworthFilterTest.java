package com.atom.bluetoothfitnessapplication.dsp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ButterworthFilterTest {

    @Test
    public void testButterworthCoefficients2ndOrder() {
        float cutoff = 2.5f;
        float fs = 50.0f;
        double[][] coeffs = ButterworthFilter.computeButterworth2ndOrderCoefficients(cutoff, fs);
        
        double[] b = coeffs[0];
        double[] a = coeffs[1];

        // Verify b0 == b2
        assertEquals(b[0], b[2], 1e-6);
        // Verify a0 == 1.0
        assertEquals(1.0, a[0], 1e-6);
        // Verify DC gain = sum(b) / sum(a) == 1.0
        double sumB = b[0] + b[1] + b[2];
        double sumA = a[0] + a[1] + a[2];
        assertEquals(1.0, sumB / sumA, 1e-4);
    }

    @Test
    public void testFilterPreservesPassbandAmplitude() {
        float fs = 50.0f;
        float cutoff = 2.5f;
        int numSamples = 200;

        // Generate a 1 Hz sine wave (well within 2.5 Hz passband) with amplitude 30.0
        float[] signal = new float[numSamples];
        float inputAmp = 30.0f;
        for (int i = 0; i < numSamples; i++) {
            signal[i] = (float) (inputAmp * Math.sin(2 * Math.PI * 1.0 * i / fs));
        }

        float[] filtered = ButterworthFilter.filterSignal(signal, cutoff, fs);

        // Find max amplitude in the middle portion (avoiding boundaries)
        float maxFilteredAmp = 0f;
        for (int i = 50; i < 150; i++) {
            if (Math.abs(filtered[i]) > maxFilteredAmp) {
                maxFilteredAmp = Math.abs(filtered[i]);
            }
        }

        // Passband signal (1 Hz) should preserve at least 90% of its amplitude (>= 27.0)
        assertTrue("Filtered amplitude (" + maxFilteredAmp + ") should retain >90% of passband amplitude (" + inputAmp + ")",
                maxFilteredAmp >= inputAmp * 0.90f);
    }

    @Test
    public void testFilterAttenuatesHighFrequencyNoise() {
        float fs = 50.0f;
        float cutoff = 2.5f;
        int numSamples = 200;

        // Generate a 15 Hz high-frequency noise sine wave (far above 2.5 Hz cutoff)
        float[] signal = new float[numSamples];
        float inputAmp = 30.0f;
        for (int i = 0; i < numSamples; i++) {
            signal[i] = (float) (inputAmp * Math.sin(2 * Math.PI * 15.0 * i / fs));
        }

        float[] filtered = ButterworthFilter.filterSignal(signal, cutoff, fs);

        float maxFilteredAmp = 0f;
        for (int i = 50; i < 150; i++) {
            if (Math.abs(filtered[i]) > maxFilteredAmp) {
                maxFilteredAmp = Math.abs(filtered[i]);
            }
        }

        // Stopband signal (15 Hz) should be heavily attenuated (>80% reduction)
        assertTrue("High frequency signal should be attenuated below 20% of original amplitude",
                maxFilteredAmp <= inputAmp * 0.20f);
    }
}
