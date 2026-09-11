package com.atom.bluetoothfitnessapplication.data.models;

import androidx.room.ColumnInfo;

public class ExerciseStats {
    @ColumnInfo(name = "exercise_type")
    private String exerciseType;

    @ColumnInfo(name = "total_sessions")
    private int totalSessions;

    @ColumnInfo(name = "last_date")
    private String lastDate;

    public ExerciseStats(String exerciseType, int totalSessions, String lastDate) {
        this.exerciseType = exerciseType;
        this.totalSessions = totalSessions;
        this.lastDate = lastDate;
    }

    public String getExerciseType() { return exerciseType; }
    public int getTotalSessions() { return totalSessions; }
    public String getLastDate() { return lastDate; }
}
