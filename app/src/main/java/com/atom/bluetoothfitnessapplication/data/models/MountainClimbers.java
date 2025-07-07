package com.atom.bluetoothfitnessapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.atom.bluetoothfitnessapplication.data.interfaces.SensorData;

import java.util.Objects;


@Entity(tableName = "mountain_climbers")
public final class MountainClimbers implements SensorData {


    @PrimaryKey(autoGenerate = true) private int id;
    @ColumnInfo(name = "acceleration_x") private String accX;
    @ColumnInfo(name = "acceleration_y") private String accY;
    @ColumnInfo(name = "acceleration_z") private String accZ;

    @ColumnInfo(name = "gyro_x") private String gyroX;
    @ColumnInfo(name = "gyro_y") private String gyroY;
    @ColumnInfo(name = "gyro_z") private String gyroZ;
    @ColumnInfo(name = "time") private String time;
    @ColumnInfo(name = "date_of_exercise") private String dateOfExercise;


    public MountainClimbers(String accX, String accY, String accZ,
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

    public void setGyroX(String gyroX) {
        this.gyroX = gyroX;
    }

    public void setGyroY(String gyroY) {
        this.gyroY = gyroY;
    }

    public void setGyroZ(String gyroZ) {
        this.gyroZ = gyroZ;
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
        if (!(o instanceof MountainClimbers that)) return false;
        return getId() == that.getId() &&
                Objects.equals(getAccX(), that.getAccX()) &&
                Objects.equals(getAccY(), that.getAccY()) &&
                Objects.equals(getAccZ(), that.getAccZ()) &&
                Objects.equals(getGyroX(), that.getGyroX()) &&
                Objects.equals(getGyroY(), that.getGyroY()) &&
                Objects.equals(getGyroZ(), that.getGyroZ()) &&
                Objects.equals(getTime(), that.getTime()) &&
                Objects.equals(getDateOfExercise(), that.getDateOfExercise());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAccX(), getAccY(), getAccZ(),
                getGyroX(), getGyroY(), getGyroZ(), getTime(), getDateOfExercise());
    }

    @NonNull
    @Override
    public String toString() {
        return "MountainClimbers{" +
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
