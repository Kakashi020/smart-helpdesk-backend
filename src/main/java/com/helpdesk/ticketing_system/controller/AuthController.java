package com.helpdesk.ticketing_system.controller;

import com.helpdesk.ticketing_system.entity.User;
import com.helpdesk.ticketing_system.entity.Admin;
import com.helpdesk.ticketing_system.entity.Profile;
import com.helpdesk.ticketing_system.repository.UserRepository;
import com.helpdesk.ticketing_system.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "https://smarthelpdeskticketingsolution.netlify.app")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    // --- 1. CUSTOMER LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(401).body("Invalid Customer Credentials");
    }

    // --- 2. STAFF / ADMIN LOGIN ---
    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isPresent() && adminOpt.get().getPassword().equals(password)) {
            return ResponseEntity.ok(adminOpt.get());
        }
        return ResponseEntity.status(401).body("Admin Access Denied");
    }

    // --- 3. CUSTOMER REGISTRATION ---
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use!");
        }

        if (user.getProfile() == null) {
            Profile newProfile = new Profile();
            newProfile.setUser(user);
            user.setProfile(newProfile);
        } else {
            user.getProfile().setUser(user);
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    // --- 4. UPDATE PROFILE (New) ---
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(user -> {
            user.setFullName(userDetails.getFullName());
            // Syncing the updated data back to the database
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(updatedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- 5. DELETE USER (For Admin Management) ---
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            userRepository.delete(user);
            return ResponseEntity.ok("User successfully terminated from system.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- 6. GET ALL CUSTOMERS ---
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}