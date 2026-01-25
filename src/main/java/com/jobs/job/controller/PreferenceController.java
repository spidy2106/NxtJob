package com.jobs.job.controller;

import com.jobs.job.entity.UserPreference;
import com.jobs.job.service.onboarding.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final UserPreferenceService service;

    @PostMapping
    public UserPreference save(
            @PathVariable Long userId,
            @RequestBody UserPreference pref) {

        return service.save(userId, pref);
    }

    @GetMapping
    public UserPreference get(@PathVariable Long userId) {
        return service.getByUser(userId);
    }

    @PutMapping
    public UserPreference update(
            @PathVariable Long userId,
            @RequestBody UserPreference pref) {

        return service.update(userId, pref);
    }
}

