package com.westminster.ticketing_system.controllers;

import com.westminster.ticketing_system.models.Configuration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "http://localhost:4200")
public class ConfigurationController {
    private static final String CONFIG_FILE_PATH = "ticket-config.json";

    @PostMapping("/save")
    public String saveConfiguration(@RequestBody Configuration config) {
        try {
            config.saveConfiguration(CONFIG_FILE_PATH);
            return "Configuration saved successfully!";
        } catch (IOException e) {
            return "Failed to save configuration: " + e.getMessage();
        }
    }

    @GetMapping("/load")
    public Configuration loadConfiguration() {
        try {
            return Configuration.loadConfiguration(CONFIG_FILE_PATH);
        } catch (IOException e) {
            // Return a default configuration or handle the error
            return new Configuration(100, 5, 2, 500);
        }
    }
}
