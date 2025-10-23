package com.bookread.controller;

import com.bookread.dto.ApiResponse;
import com.bookread.service.UserService;
import com.bookread.dto.UserCreateDTO;
import com.bookread.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO user) {
        try {
            UserDTO userDto = userService.createUser(
                    user.name(),
                    user.email(),
                    user.password()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(ex.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.getUsers());
    }
}
