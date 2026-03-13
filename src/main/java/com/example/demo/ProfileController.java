package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProfile(
            @RequestBody UserProfile profile,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");

        if (profileRepository.existsByUserId(userId)) {
            response.put("success", false);
            response.put("message", "Profile аль хэдийн үүссэн байна!");
            return ResponseEntity.badRequest().body(response);
        }

        profile.setUserId(userId);
        UserProfile saved = profileRepository.save(profile);

        response.put("success", true);
        response.put("message", "Profile амжилттай үүслээ!");
        response.put("data", saved);

        return ResponseEntity.ok(response);
    }

    // ==================== READ (My Profile) ====================
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");

        Optional<UserProfile> profileOpt = profileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Profile олдсонгүй!");
            return ResponseEntity.status(404).body(response);
        }

        response.put("success", true);
        response.put("data", profileOpt.get());

        return ResponseEntity.ok(response);
    }

    // ==================== READ (By ID) ====================
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfileById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Optional<UserProfile> profileOpt = profileRepository.findById(id);

        if (profileOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Profile олдсонгүй!");
            return ResponseEntity.status(404).body(response);
        }

        response.put("success", true);
        response.put("data", profileOpt.get());

        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @PathVariable Long id,
            @RequestBody UserProfile updatedProfile,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");

        Optional<UserProfile> profileOpt = profileRepository.findById(id);

        if (profileOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Profile олдсонгүй!");
            return ResponseEntity.status(404).body(response);
        }

        UserProfile profile = profileOpt.get();

        // Зөвхөн өөрийн profile засах боломжтой
        if (!profile.getUserId().equals(userId)) {
            response.put("success", false);
            response.put("message", "Зөвхөн өөрийн profile засах боломжтой!");
            return ResponseEntity.status(403).body(response);
        }

        profile.setName(updatedProfile.getName());
        profile.setEmail(updatedProfile.getEmail());
        profile.setBio(updatedProfile.getBio());
        profile.setPhone(updatedProfile.getPhone());
        profile.setAddress(updatedProfile.getAddress());

        UserProfile saved = profileRepository.save(profile);

        response.put("success", true);
        response.put("message", "Profile амжилттай шинэчлэгдлээ!");
        response.put("data", saved);

        return ResponseEntity.ok(response);
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProfile(
            @PathVariable Long id,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        Long userId = (Long) request.getAttribute("userId");

        Optional<UserProfile> profileOpt = profileRepository.findById(id);

        if (profileOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Profile олдсонгүй!");
            return ResponseEntity.status(404).body(response);
        }

        UserProfile profile = profileOpt.get();

        if (!profile.getUserId().equals(userId)) {
            response.put("success", false);
            response.put("message", "Зөвхөн өөрийн profile устгах боломжтой!");
            return ResponseEntity.status(403).body(response);
        }

        profileRepository.delete(profile);

        response.put("success", true);
        response.put("message", "Profile амжилттай устлаа!");

        return ResponseEntity.ok(response);
    }
}