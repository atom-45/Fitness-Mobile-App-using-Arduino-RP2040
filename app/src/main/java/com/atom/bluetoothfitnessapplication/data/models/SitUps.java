package com.atom.bluetoothfitnessapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.atom.bluetoothfitnessapplication.data.interfaces.SensorData;

import java.util.Objects;

@Entity(tableName = "situps")
public final class SitUps implements SensorData {


    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "acceleration_x") private String accX;
    @ColumnInfo(name = "acceleration_y") private String accY;
    @ColumnInfo(name = "acceleration_z") private String accZ;

    @ColumnInfo(name = "gyro_x") private String gyroX;
    @ColumnInfo(name = "gyro_y") private String gyroY;
    @ColumnInfo(name = "gyro_z") private String gyroZ;

    @ColumnInfo(name = "time") private String time;
    @ColumnInfo(name = "date_of_exercise") private String dateOfExercise;

    public SitUps(String accX, String accY, String accZ,
                  String gyroX, String gyroY, String gyroZ,
                  String time, String dateOfExercise) {

        this.accX = accX;
        this.accY = accY;
        this.accZ = accZ;

        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;

        this.time = time;
        this.dateOfExercise = dateOfExercise;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getGyroX() {
        return gyroX;
    }

    public void setGyroX(String gyroX) {
        this.gyroX = gyroX;
    }

    @Override
    public String getGyroY() {
        return gyroY;
    }

    public void setGyroY(String gyroY) {
        this.gyroY = gyroY;
    }

    @Override
    public String getGyroZ() {
        return gyroZ;
    }

    public void setGyroZ(String gyroZ) {
        this.gyroZ = gyroZ;
    }

    @Override
    public String getAccX() {
        return accX;
    }

    @Override
    public String getAccY() {
        return accY;
    }

    @Override
    public String getAccZ() {
        return accZ;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
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

    public void setTime(String time) {
        this.time = time;
    }



    public void setDateOfExercise(String dateOfExercise) {
        this.dateOfExercise = dateOfExercise;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SitUps sitUps)) return false;
        return getId() == sitUps.getId() &&
                Objects.equals(getAccX(), sitUps.getAccX()) &&
                Objects.equals(getAccY(), sitUps.getAccY()) &&
                Objects.equals(getAccZ(), sitUps.getAccZ()) &&
                Objects.equals(getTime(), sitUps.getTime()) &&
                Objects.equals(getDateOfExercise(), sitUps.getDateOfExercise()) &&
                Objects.equals(getGyroX(), sitUps.getGyroX()) &&
                Objects.equals(getGyroY(), sitUps.getGyroY()) &&
                Objects.equals(getGyroZ(), sitUps.getGyroZ());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccX(), getAccY(), getAccZ(),
                getTime(), getDateOfExercise(), getGyroX(), getGyroY(), getGyroZ());
    }

    @NonNull
    @Override
    public String toString() {
        return "SitUps{" +
                "id=" + id +
                ", accX='" + accX + '\'' +
                ", accY='" + accY + '\'' +
                ", accZ='" + accZ + '\'' +
                ", time='" + time + '\'' +
                ", dateOfExercise='" + dateOfExercise + '\'' +
                ", gyroX='" + gyroX + '\'' +
                ", gyroY='" + gyroY + '\'' +
                ", gyroZ='" + gyroZ + '\'' +
                '}';
    }
}
