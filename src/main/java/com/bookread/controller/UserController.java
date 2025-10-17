package com.bookread.controller;

import com.bookread.dto.ApiError;
import com.bookread.model.User;
import com.bookread.service.UserService;
import com.bookread.dto.UserCreateDTO;
import com.bookread.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> createUser(@RequestBody UserCreateDTO userDto) {
        try {
            UserDTO dto = userService.createUser(
                    userDto.getName(),
                    userDto.getEmail(),
                    userDto.getPassword()
            );
            return  ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError("Bad Request", ex.getMessage()));
        }
    }

    @GetMapping("/all")
    public List<User> getUsers() {
        return userService.getUsers();
    }
}
