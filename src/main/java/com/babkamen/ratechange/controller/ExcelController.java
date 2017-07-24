package com.babkamen.ratechange.controller;

import com.babkamen.ratechange.domain.Rate;
import com.babkamen.ratechange.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class ExcelController {

    @Autowired
    ExcelService excelService;

    @RequestMapping(value = "/excel",method = RequestMethod.POST)
    public void convertToExcel(@RequestBody List<Rate> tableBody, HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=Rates.xlsx");
        try {
            excelService.generateExcel(tableBody,response.getOutputStream());
        } catch (IOException e) {
            log.error("Error when converting to excel: {}", e);
        }
    }
}
