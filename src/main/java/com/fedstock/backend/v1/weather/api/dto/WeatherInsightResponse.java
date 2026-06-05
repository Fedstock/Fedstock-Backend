package com.fedstock.backend.v1.weather.api.dto;

public record WeatherInsightResponse(
    String location,
    String condition,
    String temperature,
    String message,
    String insight,
    String source,
    String forecastDate
) {
}
