package com.ahmedorabi.bluetoothapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.ahmedorabi.bluetoothapp.data.Admin;
import com.ahmedorabi.bluetoothapp.data.db.AdminDatabase;
import com.ahmedorabi.bluetoothapp.data.db.UserDao;
import com.ahmedorabi.bluetoothapp.databinding.ActivitySearchBinding;

import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActivitySearchBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_search);


        binding.searchBtn.setOnClickListener(v -> {

            String mobileNum = Objects.requireNonNull(binding.searchEd.getText()).toString();

            UserDao dao = AdminDatabase.getDatabase(getApplicationContext()).userDao();

            Admin admin = dao.getAllUsersByMobile(mobileNum);

            if (admin != null) {
                Log.e(TAG, admin.toString());
                String result = "Name : " + admin.getName() + "\n" + "Date Of Birth : " + admin.getDateOfBirth() + "\n" + "Company : " + admin.getCompany();
                binding.resultTv.setText(result);
            }else {
                binding.resultTv.setText("Not Found");

            }


        });


    }
}