package com.bookread.controller;

import com.bookread.dto.LoginRequestDTO;
import com.bookread.dto.LoginResponseDTO;
import com.bookread.model.User;
import com.bookread.security.JwtUtil;
import com.bookread.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        User user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getEmail(), String.valueOf(user.getUserLevel()));
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

}