package com.atom.bluetoothfitnessapplication.data.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_summaries")
public class WorkoutSummary {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "exercise_type")
    private String exerciseType;

    @ColumnInfo(name = "rep_count")
    private int repCount;

    @ColumnInfo(name = "max_power")
    private float maxPower;

    @ColumnInfo(name = "avg_power")
    private float avgPower;

    @ColumnInfo(name = "consistency")
    private float consistency;

    @ColumnInfo(name = "cadence")
    private float cadence;

    @ColumnInfo(name = "duration")
    private long duration;

    @ColumnInfo(name = "timestamp")
    private String timestamp;

    @ColumnInfo(name = "stability_score")
    private float stabilityScore;

    @ColumnInfo(name = "symmetry_score")
    private float symmetryScore;

    public WorkoutSummary(String exerciseType, int repCount, float maxPower, float avgPower, 
                          float consistency, float cadence, long duration, String timestamp,
                          float stabilityScore, float symmetryScore) {
        this.exerciseType = exerciseType;
        this.repCount = repCount;
        this.maxPower = maxPower;
        this.avgPower = avgPower;
        this.consistency = consistency;
        this.cadence = cadence;
        this.duration = duration;
        this.timestamp = timestamp;
        this.stabilityScore = stabilityScore;
        this.symmetryScore = symmetryScore;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getExerciseType() { return exerciseType; }
    public int getRepCount() { return repCount; }
    public float getMaxPower() { return maxPower; }
    public float getAvgPower() { return avgPower; }
    public float getConsistency() { return consistency; }
    public float getCadence() { return cadence; }
    public long getDuration() { return duration; }
    public String getTimestamp() { return timestamp; }
    public float getStabilityScore() { return stabilityScore; }
    public float getSymmetryScore() { return symmetryScore; }
}
