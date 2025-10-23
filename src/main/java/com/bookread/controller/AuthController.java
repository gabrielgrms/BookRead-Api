package com.bookread.controller;

import com.bookread.dto.*;
import com.bookread.model.RecoverPassword;
import com.bookread.model.User;
import com.bookread.security.JwtUtil;
import com.bookread.service.RecoverPasswordService;
import com.bookread.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private RecoverPasswordService recoverPasswordService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        User user = userService.authenticate(loginRequest.email(), loginRequest.password());
        if (user != null) {
            String token = jwtUtil.generateToken(user.getEmail(), String.valueOf(user.getUserLevel()));
            return ResponseEntity.ok(new TokenResponseDTO(token));
        } else {
            return ResponseEntity.status(401).body(new ApiResponse("Email ou senha inválidos"));
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody RecoverPasswordDTO recover) {
        RecoverPassword recoverPassword = recoverPasswordService.createRecoverPassword(recover.email());
        return ResponseEntity.ok(new TokenResponseDTO(recoverPassword.getToken()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("token") String token, @RequestBody PasswordRequestDTO request) {
        User user = recoverPasswordService.recoverPasswordUser(token);
        if (user != null) {
            try{
                userService.changePassword(user, request.password());
                recoverPasswordService.deleteToken(token);
                return ResponseEntity.ok(new ApiResponse("Senha alterada com sucesso"));
            } catch (Exception e){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage()));
            }
        } else {
            return ResponseEntity.status(400).body(new ApiResponse("O token está inválido"));
        }
    }

}