package com.atom.bluetoothfitnessapplication.data.models;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Encapsulates the transient state of an active workout session.
 * Designed to be held by a ViewModel to survive configuration changes.
 */
public class WorkoutSession {

    @Inject
    public WorkoutSession() {}

    private String selectedExercise = null;
    private int seconds = 0;
    private boolean running = false;
    private int liveRepCount = 0;

    // Sensor data buffers
    private final List<Float> magnitudes = new ArrayList<>();
    private final List<Float> gyroX = new ArrayList<>();
    private final List<Float> gyroY = new ArrayList<>();
    private final List<Float> gyroZ = new ArrayList<>();
    private final List<Float> gyroMagnitudes = new ArrayList<>();

    public String getSelectedExercise() { return selectedExercise; }
    public void setSelectedExercise(String exercise) { this.selectedExercise = exercise; }

    public int getSeconds() { return seconds; }
    public void incrementSeconds() { this.seconds++; }
    public void setSeconds(int seconds) { this.seconds = seconds; }

    public boolean isRunning() { return running; }
    public void setRunning(boolean running) { this.running = running; }

    public int getLiveRepCount() { return liveRepCount; }
    public void setLiveRepCount(int count) { this.liveRepCount = count; }

    public List<Float> getMagnitudes() { return magnitudes; }
    public List<Float> getGyroX() { return gyroX; }
    public List<Float> getGyroY() { return gyroY; }
    public List<Float> getGyroZ() { return gyroZ; }
    public List<Float> getGyroMagnitudes() { return gyroMagnitudes; }

    public void reset() {
        magnitudes.clear();
        gyroX.clear();
        gyroY.clear();
        gyroZ.clear();
        gyroMagnitudes.clear();
        seconds = 0;
        liveRepCount = 0;
        selectedExercise = null;
        running = false;
    }
}
