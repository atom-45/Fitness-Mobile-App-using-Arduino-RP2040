package com.atom.bluetoothfitnessapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.atom.bluetoothfitnessapplication.data.interfaces.SensorData;

import java.util.Objects;

@Entity(tableName = "pushups")
public final class PushUp implements SensorData {

    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "acceleration_x") private String accX;
    @ColumnInfo(name = "acceleration_y") private String accY;
    @ColumnInfo(name = "acceleration_z") private String accZ;

    @ColumnInfo(name = "gyro_x") private String gyroX;
    @ColumnInfo(name = "gyro_y") private String gyroY;
    @ColumnInfo(name = "gyro_z") private String gyroZ;

    @ColumnInfo(name = "time") private String time;
    @ColumnInfo(name = "date_of_exercise") private String dateOfExercise;

    public PushUp(String accX, String accY, String accZ,
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

    @Override
    public String getGyroY() {
        return gyroY;
    }

    @Override
    public String getGyroZ() {
        return gyroZ;
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

    public void setGyroX(String gyroX) {
        this.gyroX = gyroX;
    }

    public void setGyroY(String gyroY) {
        this.gyroY = gyroY;
    }

    public void setGyroZ(String gyroZ) {
        this.gyroZ = gyroZ;
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
        if (!(o instanceof PushUp pushUp)) return false;
        return getId() == pushUp.getId() &&
                Objects.equals(getAccX(), pushUp.getAccX()) &&
                Objects.equals(getAccY(), pushUp.getAccY()) &&
                Objects.equals(getAccZ(), pushUp.getAccZ()) &&
                Objects.equals(getGyroX(), pushUp.getGyroX()) &&
                Objects.equals(getGyroY(), pushUp.getGyroY()) &&
                Objects.equals(getGyroZ(), pushUp.getGyroZ()) &&
                Objects.equals(getTime(), pushUp.getTime()) &&
                Objects.equals(getDateOfExercise(), pushUp.getDateOfExercise());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccX(), getAccY(), getAccZ(),
                getGyroX(), getGyroY(), getGyroZ(), getTime(), getDateOfExercise());
    }

    @NonNull
    @Override
    public String toString() {
        return "PushUp{" +
                "id=" + id +
                ", accX='" + accX + '\'' +
                ", accY='" + accY + '\'' +
                ", accZ='" + accZ + '\'' +
                ", gyroX='" + gyroX + '\'' +
                ", gyroY='" + gyroY + '\'' +
                ", gyroZ='" + gyroZ + '\'' +
                ", time='" + time + '\'' +
                ", dateOfExercise='" + dateOfExercise + '\'' +
                '}';
    }
}
