package com.fedstock.backend.v1.localai.api;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fedstock.backend.v1.localai.application.LocalAiService;

@RestController
@RequestMapping("/api/v1/local-ai")
public class LocalAiController {

    private final LocalAiService localAiService;

    public LocalAiController(LocalAiService localAiService) {
        this.localAiService = localAiService;
    }

    @GetMapping("/health")
    public JsonNode health() {
        return localAiService.health();
    }
}
