package com.chandan.medical_store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final List<String> ALLOWED_PHONE_NUMBERS = List.of(
            "8887505623",
            "9889767593",
            "8181000306"
    );
   

        @PostMapping("/login")
        public String handleLogin(@RequestParam String username, @RequestParam String password) {
            if ("Arush".equals(username) && "Arush@3099".equals(password)) {
                return "redirect:/index.html"; // Redirection to dashboard
            }
            return "redirect:/login.html?error=true";
        }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("ROLE_EMPLOYEE");

        Map<String, String> response = new HashMap<>();
        response.put("username", username);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String phone = request.get("phone") != null ? request.get("phone").trim() : "";

        if (!ALLOWED_PHONE_NUMBERS.contains(phone)) {
            return ResponseEntity.status(403).body(Map.of("message", "Access Denied: Mobile number is not authorized!"));
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> phone.equalsIgnoreCase(u.getPhone()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mobile number not found in database!"));
        }

        User user = userOpt.get();
        
        String generatedOtp = String.format("%06d", new Random().nextInt(900000) + 100000);
        user.setOtp(generatedOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        System.out.println("\n==========================================");
        System.out.println("SECURITY OTP GENERATED FOR " + phone + " : " + generatedOtp);
        System.out.println("==========================================\n");

        return ResponseEntity.ok(Map.of("message", "OTP generated! Check Eclipse Console log for OTP."));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtpAndReset(@RequestBody Map<String, String> request) {
        String phone = request.get("phone") != null ? request.get("phone").trim() : "";
        String otp = request.get("otp") != null ? request.get("otp").trim() : "";
        String newPassword = request.get("newPassword") != null ? request.get("newPassword").trim() : "";

        if (!ALLOWED_PHONE_NUMBERS.contains(phone)) {
            return ResponseEntity.status(403).body(Map.of("message", "Access Denied!"));
        }

        Optional<User> userOpt = userRepository.findAll().stream()
                .filter(u -> phone.equalsIgnoreCase(u.getPhone()))
                .findFirst();

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User account not found!"));
        }

        User user = userOpt.get();

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP!"));
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP has expired!"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully! You can now login."));
        
    }
}