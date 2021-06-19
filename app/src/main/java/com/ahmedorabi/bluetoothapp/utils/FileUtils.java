package com.ahmedorabi.bluetoothapp.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.ahmedorabi.bluetoothapp.data.Admin;
import com.ahmedorabi.bluetoothapp.data.GenerateAdmins;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";


    public static void createExcelSheet(File filePath) {

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        HSSFSheet sheet = hssfWorkbook.createSheet("Custom Sheet");

        HSSFRow rowhead = sheet.createRow((short) 0);
        rowhead.createCell(0).setCellValue("Name");
        rowhead.createCell(1).setCellValue("Mobile Number");
        rowhead.createCell(2).setCellValue("Date Of Birth");
        rowhead.createCell(3).setCellValue("working Company");

        List<Admin> admins = GenerateAdmins.getAdmins();

        for (int i = 1; i <= 5; i++) {
            HSSFRow row = sheet.createRow((short) i);
            Admin admin = admins.get(i - 1);
            row.createCell(0).setCellValue(admin.getName());
            row.createCell(1).setCellValue(admin.getMobile());
            row.createCell(2).setCellValue(admin.getDateOfBirth());
            row.createCell(3).setCellValue(admin.getCompany());
        }


        try {
            if (!filePath.exists()) {
                filePath.createNewFile();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static List<Admin> readExcel(Activity activity, File file) {
        List<Admin> adminList = new ArrayList<>();

        if (file == null) {
            Log.e("NullFile", "read Excel Error, file is empty");
            return adminList;
        }

        try {
            InputStream stream = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(stream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

            String name = "", mobile = "", dateOfBirth = "", company = "";


            for (int r = 1; r < rowsCount; r++) {
                Row row = sheet.getRow(r);
                int cellsCount = row.getPhysicalNumberOfCells();
                //Read one line at a time
                for (int c = 0; c < cellsCount; c++) {
                    //Convert the contents of each grid to a string
                    String value = getCellAsString(row, c, formulaEvaluator);
                    //   String cellInfo = "r:" + r + "; c:" + c + "; v:" + value;

                    switch (c) {
                        case 0:
                            name = value;
                            break;
                        case 1:
                            mobile = value;
                            break;
                        case 2:
                            dateOfBirth = value;
                            break;
                        case 3:
                            company = value;
                            break;
                    }
                }

                adminList.add(new Admin(name, mobile, dateOfBirth, company));

            }

            Log.e(TAG, adminList.toString());

            Toast.makeText(activity, "File Read Successfully ", Toast.LENGTH_SHORT).show();


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(activity, "Problem in File Read" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        return adminList;

    }


    private static String getCellAsString(Row row, int c, FormulaEvaluator formulaEvaluator) {
        String value = "";
        try {
            Cell cell = row.getCell(c);
            CellValue cellValue = formulaEvaluator.evaluate(cell);

            value = "" + cellValue.getStringValue();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return value;
    }


}
