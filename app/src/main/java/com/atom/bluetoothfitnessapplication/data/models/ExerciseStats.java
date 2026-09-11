package com.atom.bluetoothfitnessapplication.data.models;

import androidx.room.ColumnInfo;

public class ExerciseStats {
    @ColumnInfo(name = "exercise_type")
    private String exerciseType;

    @ColumnInfo(name = "total_sessions")
    private int totalSessions;

    @ColumnInfo(name = "weekly_sessions")
    private int weeklySessions;

    @ColumnInfo(name = "last_date")
    private String lastDate;

    public ExerciseStats(String exerciseType, int totalSessions, int weeklySessions, String lastDate) {
        this.exerciseType = exerciseType;
        this.totalSessions = totalSessions;
        this.weeklySessions = weeklySessions;
        this.lastDate = lastDate;
    }

    public String getExerciseType() { return exerciseType; }
    public int getTotalSessions() { return totalSessions; }
    public int getWeeklySessions() { return weeklySessions; }
    public String getLastDate() { return lastDate; }
}
