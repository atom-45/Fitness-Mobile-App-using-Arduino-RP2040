package com.atom.bluetoothfitnessapplication.utilities;

import java.util.Locale;

public class TimerUtils {
    public static String formatElapsedTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d : %02d : %02d", hours, minutes, secs);
    }
}
