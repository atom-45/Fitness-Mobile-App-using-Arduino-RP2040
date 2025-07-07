package com.atom.bluetoothfitnessapplication.data.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "exercise_description")
public class ExerciseDescription {


    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "date_time")
    private String dateTime;

    @ColumnInfo(name = "description")
    private String description;

    public ExerciseDescription(String dateTime, String description) {
        this.dateTime = dateTime;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExerciseDescription that)) return false;
        return getId() == that.getId() &&
                Objects.equals(getDateTime(), that.getDateTime()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDateTime(), getDescription());
    }

    @NonNull
    @Override
    public String toString() {
        return "ExerciseDescription{" +
                "id=" + id +
                ", dateTime='" + dateTime + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
