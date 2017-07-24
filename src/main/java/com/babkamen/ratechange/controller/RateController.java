package com.babkamen.ratechange.controller;

import com.babkamen.ratechange.domain.Rate;
import com.babkamen.ratechange.dto.ExceptionResponse;
import com.babkamen.ratechange.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class RateController {

    @Autowired
    RateService service;

    @RequestMapping(path = "/rates",method = RequestMethod.GET)
    @ResponseBody
    public Object findAll(Pageable pageable){
        return service.findAll(pageable);
    }

    @RequestMapping(path = "/rates/{startDate}/{endDate}",method = RequestMethod.GET)
    @ResponseBody
    public Page<Rate> getResponseRates(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                       Pageable pageable){
        return service.findByDateBetween(startDate, endDate, pageable);
    }

    @RequestMapping(path = "/rates/download/{startDate}/{endDate}",method = RequestMethod.POST)
    public ResponseEntity downloadRates(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if(days >93){
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ExceptionResponse.builder()
                    .code(1)
                    .message("Date span between dates should be less or equal 93 days")
                    .build());
        }
        service.downloadRates(startDate,endDate);
        return ResponseEntity.ok().build();
    }

}
