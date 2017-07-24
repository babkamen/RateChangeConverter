package com.babkamen.ratechange.service;

import com.babkamen.ratechange.domain.Rate;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

@Service
public class ExcelService {

    /**
     * Generates excel and writes to output stream
     */
    public void generateExcel(List<Rate> rates, OutputStream outputStream) throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("USD exchange rates");
        sheet.setDefaultColumnWidth(30);

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Rate");


        // create data rows
        int rowCount = 1;

        for (Rate rate : rates) {
            Row aRow = sheet.createRow(rowCount++);
            aRow.createCell(0).setCellValue(rate.getDate().toString());
            aRow.createCell(1).setCellValue(rate.getRate());
        }

        wb.write(outputStream);

    }
}
