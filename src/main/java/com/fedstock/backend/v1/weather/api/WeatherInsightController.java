package com.fedstock.backend.v1.weather.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.v1.weather.api.dto.WeatherInsightResponse;
import com.fedstock.backend.v1.weather.application.WeatherInsightService;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherInsightController {

    private final WeatherInsightService weatherInsightService;

    public WeatherInsightController(WeatherInsightService weatherInsightService) {
        this.weatherInsightService = weatherInsightService;
    }

    @GetMapping("/insight")
    public WeatherInsightResponse insight(
        @RequestParam(defaultValue = "서울특별시 동작구") String location,
        @RequestParam(required = false) Double latitude,
        @RequestParam(required = false) Double longitude
    ) {
        return weatherInsightService.insight(location, latitude, longitude);
    }
}
