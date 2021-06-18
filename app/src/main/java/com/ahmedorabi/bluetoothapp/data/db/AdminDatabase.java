package com.ahmedorabi.bluetoothapp.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ahmedorabi.bluetoothapp.data.Admin;

@Database(entities = {Admin.class}, version = 1, exportSchema = false)
public abstract class AdminDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    private static AdminDatabase adminDatabase;


    public static synchronized AdminDatabase getDatabase(Context context) {

        if (adminDatabase == null) {
            adminDatabase = Room.databaseBuilder(context,
                    AdminDatabase.class, "users_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return adminDatabase;
    }


}
