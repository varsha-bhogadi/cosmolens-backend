package com.example.apod.controller;

import com.example.apod.model.ApodResponse;
import com.example.apod.service.ApodService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/apod")
@CrossOrigin(origins = "*")
public class ApodController {

    private final ApodService apodService;

    public ApodController(ApodService apodService) {
        this.apodService = apodService;
    }

    @GetMapping("/today")
    public ApodResponse getTodayApod() {
        return apodService.getTodayApod();
    }


    @GetMapping("/date")
    public ApodResponse getApodByDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return apodService.getApodForDate(date);
    }


    @GetMapping("/range")
    public List<ApodResponse> getApodRange(
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }
        return apodService.getApodRange(startDate, endDate);
    }
}
