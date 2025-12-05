package com.example.apod.service;

import com.example.apod.exception.NasaApiException;
import com.example.apod.model.ApodResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class NasaApodClient {

    private static final String APOD_URL = "https://api.nasa.gov/planetary/apod";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestTemplate restTemplate;
    private final String apiKey;

    public NasaApodClient(RestTemplate restTemplate,
                          @Value("${nasa.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;


        System.out.println("NASA API key in use (masked): " + maskKey(apiKey));
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 10) return "UNKNOWN";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    public ApodResponse getApodForDate(LocalDate date) {
        String dateParam = date.format(FORMATTER);
        String url = APOD_URL + "?api_key=" + apiKey + "&date=" + dateParam;

        return callNasaWithRetries(url);
    }

    public ApodResponse getTodayApod() {
        return getApodForDate(LocalDate.now());
    }


    private ApodResponse callNasaWithRetries(String url) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                attempt++;
                System.out.println("Calling NASA (attempt " + attempt + "): " + url);

                ApodResponse response = restTemplate.getForObject(url, ApodResponse.class);
                if (response == null) {
                    throw new NasaApiException("NASA returned empty response");
                }

                return response;

            } catch (RestClientException e) {
                System.out.println("NASA API error on attempt " + attempt + ": " + e.getMessage());

                if (attempt == maxRetries) {
                    throw new NasaApiException("NASA API failed after 3 attempts", e);
                }

                // wait before retrying
                try {
                    Thread.sleep(700);
                } catch (InterruptedException ignored) {}
            }
        }

        throw new NasaApiException("Unexpected NASA API error");
    }
}
