package com.ahmedorabi.bluetoothapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class Admin {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private String name;
    private String mobile;
    private String dateOfBirth;
    private String company;


    public Admin() {

    }


    public Admin(String name, String mobile, String dateOfBirth, String company) {
        this.name = name;
        this.mobile = mobile;
        this.dateOfBirth = dateOfBirth;
        this.company = company;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}
