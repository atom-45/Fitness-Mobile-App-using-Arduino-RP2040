package com.atom.bluetoothfitnessapplication.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.atom.bluetoothfitnessapplication.data.local.daos.ExerciseDAO;
import com.atom.bluetoothfitnessapplication.data.local.daos.TestDAO;
import com.atom.bluetoothfitnessapplication.data.models.AccelerometerData;
import com.atom.bluetoothfitnessapplication.data.models.Backs;
import com.atom.bluetoothfitnessapplication.data.models.ExerciseDescription;
import com.atom.bluetoothfitnessapplication.data.models.Flapjacks;
import com.atom.bluetoothfitnessapplication.data.models.MountainClimbers;
import com.atom.bluetoothfitnessapplication.data.models.Plank;
import com.atom.bluetoothfitnessapplication.data.models.PushUp;
import com.atom.bluetoothfitnessapplication.data.models.SitUps;
import com.atom.bluetoothfitnessapplication.data.models.Skipping;
import com.atom.bluetoothfitnessapplication.data.models.Walking;
import com.atom.bluetoothfitnessapplication.data.models.Weights;

@Database(entities = {AccelerometerData.class, ExerciseDescription.class,
        Weights.class, Skipping.class, PushUp.class, Backs.class,
        MountainClimbers.class, Flapjacks.class, SitUps.class, Walking.class, Plank.class} ,
        version = 2, exportSchema = false)
public abstract class FitnessExerciseDatabase extends RoomDatabase {

    private static volatile FitnessExerciseDatabase instance;

    public static FitnessExerciseDatabase getInstance(final Context context)
    {
        if (instance==null)
        {
            synchronized (FitnessExerciseDatabase.class) {
                if (instance==null) {
                    instance = Room.databaseBuilder(context, FitnessExerciseDatabase.class,
                            "fitness_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract TestDAO testDAO();

    public abstract ExerciseDAO exerciseDAO();


    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {

            String[] tableNames = {"backs", "weights", "skipping", "pushups",
                    "mountain_climbers", "flapjacks", "situps", "walking", "planks"};

            String[] columnNames = {"gyro_x", "gyro_y", "gyro_z"};


            for (String tableName : tableNames) {
                for (String columnName : columnNames) {
                    supportSQLiteDatabase.execSQL("ALTER TABLE " + tableName
                            + " ADD COLUMN " + columnName + " TEXT");
                }
            }

        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {

        }
    };
}
