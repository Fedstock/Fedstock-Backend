package com.fedstock.backend.v1.weather.application;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fedstock.backend.main.error.BadGatewayException;
import com.fedstock.backend.v1.weather.api.dto.WeatherInsightResponse;

@Service
public class WeatherInsightService {

    private static final double DEFAULT_LATITUDE = 37.5124;
    private static final double DEFAULT_LONGITUDE = 126.9393;

    private final RestClient restClient;

    public WeatherInsightService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://api.open-meteo.com").build();
    }

    public WeatherInsightResponse insight(String location, Double latitude, Double longitude) {
        double selectedLatitude = latitude == null ? DEFAULT_LATITUDE : latitude;
        double selectedLongitude = longitude == null ? DEFAULT_LONGITUDE : longitude;

        try {
            JsonNode response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v1/forecast")
                    .queryParam("latitude", selectedLatitude)
                    .queryParam("longitude", selectedLongitude)
                    .queryParam("current", "temperature_2m,weather_code")
                    .queryParam("timezone", "Asia/Seoul")
                    .build())
                .retrieve()
                .body(JsonNode.class);

            JsonNode current = response.path("current");
            double temperature = current.path("temperature_2m").asDouble();
            int weatherCode = current.path("weather_code").asInt();
            String condition = condition(weatherCode);
            String message = message(condition);

            return new WeatherInsightResponse(
                location,
                condition,
                Math.round(temperature) + "°C",
                message,
                insight(condition),
                "OPEN_METEO",
                LocalDate.now().toString()
            );
        } catch (RestClientException exception) {
            throw new BadGatewayException("Weather API call failed.");
        }
    }

    private String condition(int weatherCode) {
        if (weatherCode == 0) {
            return "맑음";
        }
        if (weatherCode <= 3) {
            return "흐림";
        }
        if (weatherCode >= 51 && weatherCode <= 67) {
            return "비";
        }
        if (weatherCode >= 71 && weatherCode <= 77) {
            return "눈";
        }
        if (weatherCode >= 80 && weatherCode <= 99) {
            return "소나기";
        }
        return "보통";
    }

    private String message(String condition) {
        return switch (condition) {
            case "맑음" -> "맑은 날씨라 외출 수요가 늘 수 있습니다. 아이스크림, 차가운 음료, 간식류 판매 흐름을 먼저 확인하세요.";
            case "비", "소나기" -> "비 예보가 있어 실내 취식과 간편식 수요를 확인하는 것이 좋습니다.";
            case "눈" -> "눈 예보가 있어 방문 수요 변동과 따뜻한 음료, 즉석식품 재고를 확인하세요.";
            case "흐림" -> "흐린 날씨에는 기본 수요 중심으로 재고를 안정적으로 유지하는 것이 좋습니다.";
            default -> "오늘 날씨에 맞춰 주요 상품군의 판매 흐름을 확인하세요.";
        };
    }

    private String insight(String condition) {
        return switch (condition) {
            case "맑음" -> "오늘 같은 날씨에는 아이스크림, 차가운 음료의 수요 증가가 예상됩니다. 날씨에 따른 운영 참고용 안내입니다.";
            case "비", "소나기" -> "오늘 같은 날씨에는 우산, 간편식, 실내 소비 상품의 수요 변화를 확인하세요.";
            case "눈" -> "오늘 같은 날씨에는 따뜻한 음료와 즉석식품 재고를 먼저 점검하세요.";
            default -> "날씨 기반 운영 참고용 안내입니다.";
        };
    }
}
