package com.umang.Week1.PollingController;


import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@RestController
public class PollingController {

    private final Map<String, String> statusMap = new ConcurrentHashMap<>();

    // Start mock EC2 creation
    @PostMapping("/create")
    public String createInstance(@RequestParam String id) {
        statusMap.put(id, "pending");

        // Simulate async EC2 creation
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Thread.sleep(10000); // simulate 10 seconds creation
                statusMap.put(id, "running");
            } catch (InterruptedException e) {
                statusMap.put(id, "error");
            }
        });

        return "EC2 creation started for ID: " + id;
    }

    // Short Polling: immediately return status
    @GetMapping("/status/{id}")
    public String getStatus(@PathVariable String id) {
        return statusMap.getOrDefault(id, "not_found");
    }

    // Long Polling: wait until status becomes "running" or timeout
    @GetMapping("/longpoll-status/{id}")
    public String longPollStatus(@PathVariable String id) throws InterruptedException {
        int timeoutMs = 30000;
        int intervalMs = 1000;

        for (int waited = 0; waited < timeoutMs; waited += intervalMs) {
            String status = statusMap.getOrDefault(id, "not_found");

            if ("running".equals(status) || "error".equals(status) || "not_found".equals(status)) {
                return status;
            }

            Thread.sleep(intervalMs);
        }

        return statusMap.getOrDefault(id, "not_found");
    }
}
