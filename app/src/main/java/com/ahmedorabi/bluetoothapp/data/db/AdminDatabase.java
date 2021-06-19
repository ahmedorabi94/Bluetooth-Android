package com.ahmedorabi.bluetoothapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ahmedorabi.bluetoothapp.data.Admin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Admin.class}, version = 1, exportSchema = false)
public abstract class AdminDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static AdminDatabase adminDatabase;
    private static final int NUMBER_OF_THREADS = 4;
  public   static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static synchronized AdminDatabase getDatabase(Context context) {

        if (adminDatabase == null) {
            adminDatabase = Room.databaseBuilder(context,
                    AdminDatabase.class, "users_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return adminDatabase;
    }


}
