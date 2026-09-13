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
import com.atom.bluetoothfitnessapplication.data.models.WorkoutSummary;

@Database(entities = {AccelerometerData.class, ExerciseDescription.class,
        Weights.class, Skipping.class, PushUp.class, Backs.class,
        MountainClimbers.class, Flapjacks.class, SitUps.class, Walking.class, Plank.class,
        WorkoutSummary.class} ,
        version = 5, exportSchema = false)
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
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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
            supportSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS `workout_summaries` " +
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`exercise_type` TEXT, `rep_count` INTEGER NOT NULL, " +
                    "`max_power` REAL NOT NULL, `avg_power` REAL NOT NULL, " +
                    "`consistency` REAL NOT NULL, `cadence` REAL NOT NULL, " +
                    "`duration` INTEGER NOT NULL, `timestamp` TEXT)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("ALTER TABLE `workout_summaries` ADD COLUMN `stability_score` REAL NOT NULL DEFAULT 0.0");
            supportSQLiteDatabase.execSQL("ALTER TABLE `workout_summaries` ADD COLUMN `symmetry_score` REAL NOT NULL DEFAULT 0.0");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4,5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase supportSQLiteDatabase) {
            supportSQLiteDatabase.execSQL("ALTER TABLE `workout_summaries` ADD COLUMN `range_of_motion` REAL NOT NULL DEFAULT 0.0");
        }
    };
}
