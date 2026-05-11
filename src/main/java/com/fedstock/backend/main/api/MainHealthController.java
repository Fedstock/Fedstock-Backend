package com.fedstock.backend.main.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainHealthController {

    @GetMapping("/health")
    public MainHealthResponse health() {
        return new MainHealthResponse(true);
    }
}
