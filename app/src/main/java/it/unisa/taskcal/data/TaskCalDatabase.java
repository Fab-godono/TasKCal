package it.unisa.taskcal.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Event.class}, version = 1, exportSchema = false)
public abstract class TaskCalDatabase extends RoomDatabase {

    public abstract EventDao eventDao();

    private static volatile TaskCalDatabase INSTANCE;

    public static TaskCalDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskCalDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TaskCalDatabase.class,
                                    "taskcal.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}