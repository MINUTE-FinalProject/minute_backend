package com.minute.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {
    @Value("${weather.api.key}")
    private String apiKey;
    private final RestTemplate restTemplate;

    // 지역명
    @GetMapping("/geocode")
    public ResponseEntity<String> geocode(
            @RequestParam double lat,
            @RequestParam double lon) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.openweathermap.org/geo/1.0/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("limit", 1)
                .queryParam("appid", apiKey)
                .toUriString();

        String body = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(body);
    }

    // 1) 위도/경도로 현재 날씨 가져오기
    @GetMapping("/current")
    public ResponseEntity<String> getCurrent(
            @RequestParam double lat,
            @RequestParam double lon) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.openweathermap.org/data/2.5/weather")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .queryParam("appid", apiKey)
                .toUriString();

        String body = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(body);
    }

    // 2) 위도/경도로 5일 예보 가져오기
    @GetMapping("/forecast")
    public ResponseEntity<String> getForecast(
            @RequestParam double lat,
            @RequestParam double lon) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://api.openweathermap.org/data/2.5/forecast")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("units", "metric")
                .queryParam("lang", "kr")
                .queryParam("appid", apiKey)
                .toUriString();

        String body = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(body);
    }

}

