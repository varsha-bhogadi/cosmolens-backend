package com.example.apod.service;

import com.example.apod.model.ApodResponse;
import com.example.apod.model.CacheEntry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApodService {

    private final NasaApodClient nasaApodClient;

    // Simple in-memory cache
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private final Duration ttl = Duration.ofMinutes(10); // cache expiry time
    private final int maxSize = 100;                     // max cache size

    public ApodService(NasaApodClient nasaApodClient) {
        this.nasaApodClient = nasaApodClient;
    }

    public ApodResponse getTodayApod() {
        LocalDate today = LocalDate.now();
        return getApodForDate(today);
    }

    public ApodResponse getApodForDate(LocalDate date) {
        String key = date.toString();
        ApodResponse cached = getFromCache(key);
        if (cached != null) {
            return cached;
        }

        ApodResponse fromApi = nasaApodClient.getApodForDate(date);
        putInCache(key, fromApi);
        return fromApi;
    }

    public List<ApodResponse> getApodRange(LocalDate startDate, LocalDate endDate) {
        List<ApodResponse> result = new ArrayList<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            result.add(getApodForDate(current));
            current = current.plusDays(1);
        }

        return result;
    }

    private ApodResponse getFromCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        Instant now = Instant.now();
        if (now.isAfter(entry.getCachedAt().plus(ttl))) {
            cache.remove(key);
            return null;
        }

        return entry.getResponse();
    }

    private void putInCache(String key, ApodResponse value) {
        if (cache.size() >= maxSize) {
            evictOldestEntry();
        }
        cache.put(key, new CacheEntry(value, Instant.now()));
    }

    private void evictOldestEntry() {
        cache.entrySet().stream()
                .min(Comparator.comparing(e -> e.getValue().getCachedAt()))
                .map(Map.Entry::getKey)
                .ifPresent(cache::remove);
    }
}
