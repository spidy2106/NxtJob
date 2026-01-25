package com.jobs.job.service.onboarding;

import com.jobs.job.entity.User;
import com.jobs.job.entity.UserPreference;
import com.jobs.job.repository.UserPreferenceRepository;
import com.jobs.job.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserPreference save(Long userId, UserPreference pref) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreference existing = preferenceRepository
                .findByUser_Id(userId)
                .orElse(new UserPreference());

        existing.setUser(user);
        existing.setPreferredRoles(pref.getPreferredRoles());
        existing.setPreferredLocations(pref.getPreferredLocations());
        existing.setTechStack(pref.getTechStack());
        existing.setMinMatchScore(pref.getMinMatchScore());
        existing.setMaxAlertsPerDay(pref.getMaxAlertsPerDay());
        existing.setRemoteOnly(pref.isRemoteOnly());

        return preferenceRepository.save(existing);
    }


    public UserPreference getByUser(Long userId) {

        return preferenceRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Preferences not found"));
    }


        public UserPreference update(Long userId, UserPreference incoming) {


            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserPreference existing = preferenceRepository
                    .findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Preferences not found"));


            existing.setPreferredRoles(incoming.getPreferredRoles());
            existing.setPreferredLocations(incoming.getPreferredLocations());
            existing.setTechStack(incoming.getTechStack());
            existing.setMinMatchScore(incoming.getMinMatchScore());
            existing.setMaxAlertsPerDay(incoming.getMaxAlertsPerDay());
            existing.setRemoteOnly(incoming.isRemoteOnly());

            return preferenceRepository.save(existing);
        }
}
