package com.ahmedorabi.bluetoothapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ahmedorabi.bluetoothapp.data.Admin;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(Admin user);

    @Insert
    void insertAll(List<Admin> users);

    @Query("SELECT * from user")
    LiveData<List<Admin>> getAllUsers();

    @Query("SELECT * from user where mobile = :mobile")
    Admin getAllUsersByMobile(String mobile);

    @Query("DELETE FROM user WHERE name = :id")
    void deleteUser(String id);

    @Query("Delete FROM user")
    void deleteAllUsers();


}