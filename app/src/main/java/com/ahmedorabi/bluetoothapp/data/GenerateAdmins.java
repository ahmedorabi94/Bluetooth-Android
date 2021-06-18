package com.ahmedorabi.bluetoothapp.data;

import java.util.ArrayList;
import java.util.List;

public class GenerateAdmins {


    public static List<Admin> getAdmins() {

        List<Admin> admins = new ArrayList<>();

        admins.add(new Admin("Ahmed", "01011135949", "17/3/1994", "Idemia"));
        admins.add(new Admin("Mahmoud", "01022235949", "17/5/1995", "Revival"));
        admins.add(new Admin("Ali", "01033335949", "12/6/1996", "Freelance"));
        admins.add(new Admin("Orabi", "01044435949", "19/8/1997", "Zagazig"));
        admins.add(new Admin("Wafaa", "01055535949", "10/9/1998", "Pharmacy"));


        return admins;
    }
}
