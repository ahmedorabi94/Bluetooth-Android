package com.ahmedorabi.bluetoothapp.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ahmedorabi.bluetoothapp.data.Admin;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface UserDao {

    @Insert
    void insert(Admin user);

    @Insert
    void insertAll(List<Admin> users);

    @Query("SELECT * from user")
    LiveData<List<Admin>> getAllUsers();

    @Query("SELECT * from user where mobile = :mobile")
    LiveData<Admin> getUserByMobile(String mobile);

    @Query("DELETE FROM user WHERE name = :id")
    void deleteUser(String id);

    @Query("Delete FROM user")
    void deleteAllUsers();


}