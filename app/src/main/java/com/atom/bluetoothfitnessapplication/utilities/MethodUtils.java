package com.atom.bluetoothfitnessapplication.utilities;

import android.util.Log;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Largest-Triangle-Three-Buckets (LTTB) downsampling algorithm.
 * This algorithm is designed to reduce the number of data points in a series
 * while preserving its visual characteristics, like peaks and troughs.
 */

public class MethodUtils {

    /**
     * Downsamples a list of Entry objects using the LTTB algorithm.
     *
     * @param data The original list of data points to be downsampled.
     * @param targetDataPoints The desired number of data points after sampling.
     * @return A new list containing the downsampled data points.
     */
    public static List<Entry> downsample(List<Entry> data, int targetDataPoints) {
        // Guard against invalid input
        if (targetDataPoints < 2 || targetDataPoints >= data.size()) {
            return data; // Return original data if target is invalid or no sampling is needed
        }

        List<Entry> sampled = new ArrayList<>(targetDataPoints);

        // Calculate the size of each bucket
        double every = (double) (data.size() - 2) / (targetDataPoints - 2);

        int a = 0; // Index of the first point in the current bucket
        int next_a = 0;

        // Always add the very first point
        sampled.add(data.get(a));

        for (int i = 0; i < targetDataPoints - 2; i++) {
            // Calculate the average point for the next bucket
            double avg_x = 0;
            double avg_y = 0;
            int avg_range_start = (int) (Math.floor((i + 1) * every) + 1);
            int avg_range_end = (int) (Math.floor((i + 2) * every) + 1);
            if (avg_range_end > data.size()) {
                avg_range_end = data.size();
            }

            int avg_range_length = avg_range_end - avg_range_start;

            for (; avg_range_start < avg_range_end; avg_range_start++) {
                avg_x += data.get(avg_range_start).getX();
                avg_y += data.get(avg_range_start).getY();
            }
            avg_x /= avg_range_length;
            avg_y /= avg_range_length;

            // Get the range for the current bucket
            int range_offs = (int) (Math.floor(i * every) + 1);
            int range_to = (int) (Math.floor((i + 1) * every) + 1);

            // Point a is the last selected point or the first point
            Entry pointA = data.get(a);

            double max_area = -1;
            Entry nextPoint = null;

            for (; range_offs < range_to; range_offs++) {
                // Calculate the area of the triangle formed by point A, the current point, and the average of the next bucket
                double area = Math.abs(
                        (pointA.getX() - avg_x) * (data.get(range_offs).getY() - pointA.getY()) -
                                (pointA.getX() - data.get(range_offs).getX()) * (avg_y - pointA.getY())
                ) * 0.5;

                if (area > max_area) {
                    max_area = area;
                    nextPoint = data.get(range_offs);
                    next_a = range_offs; // Keep track of the index of the best point
                }
            }

            // Add the selected point
            if (nextPoint != null) {
                sampled.add(nextPoint);
            }
            a = next_a; // Update the starting point for the next iteration
        }

        // Always add the very last point
        sampled.add(data.get(data.size() - 1));

        return sampled;
    }

}


