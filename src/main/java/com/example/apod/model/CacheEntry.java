package com.example.apod.model;

import java.time.Instant;

public class CacheEntry {

    private ApodResponse response;
    private Instant cachedAt;

    public CacheEntry(ApodResponse response, Instant cachedAt) {
        this.response = response;
        this.cachedAt = cachedAt;
    }

    public ApodResponse getResponse() {
        return response;
    }

    public Instant getCachedAt() {
        return cachedAt;
    }
}
