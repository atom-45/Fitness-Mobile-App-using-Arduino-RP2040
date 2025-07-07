package com.atom.bluetoothfitnessapplication.data.models;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "test_table")
public class AccelerometerData {

    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "acceleration_x") private String accX;
    @ColumnInfo(name = "acceleration_y") private String accY;
    @ColumnInfo(name = "acceleration_z") private String accZ;

    @ColumnInfo(name = "start_time") private String startTime;
    @ColumnInfo(name = "end_time") private String endTime;
    @ColumnInfo(name = "date_of_exercise") private String dateOfExercise;

    public AccelerometerData(String accX, String accY, String accZ,
                             String startTime, String endTime, String dateOfExercise)
    {
        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateOfExercise = dateOfExercise;
    }

    public int getId() {
        return id;
    }

    public String getAccX() {
        return accX;
    }

    public String getAccY() {
        return accY;
    }

    public String getAccZ() {
        return accZ;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDateOfExercise() {
        return dateOfExercise;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccX(String accX) {
        this.accX = accX;
    }

    public void setAccY(String accY) {
        this.accY = accY;
    }

    public void setAccZ(String accZ) {
        this.accZ = accZ;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDateOfExercise(String dateOfExercise) {
        this.dateOfExercise = dateOfExercise;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccelerometerData that)) return false;
        return getId() == that.getId() && Objects.equals(getAccX(), that.getAccX()) &&
                Objects.equals(getAccY(), that.getAccY()) &&
                Objects.equals(getAccZ(), that.getAccZ()) &&
                Objects.equals(getStartTime(), that.getStartTime()) &&
                Objects.equals(getEndTime(), that.getEndTime()) &&
                Objects.equals(getDateOfExercise(), that.getDateOfExercise());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccX(), getAccY(), getAccZ(),
                getStartTime(), getEndTime(), getDateOfExercise());
    }

    @NonNull
    @Override
    public String toString() {
        return "AccelerometerData{" +
                "id=" + id +
                ", accX='" + accX + '\'' +
                ", accY='" + accY + '\'' +
                ", accZ='" + accZ + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", dateOfExercise='" + dateOfExercise + '\'' +
                '}';
    }
}
