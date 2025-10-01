package com.bookread.controller;

import com.bookread.model.User;
import com.bookread.service.UserService;
import com.bookread.dto.UserCreateDTO;
import com.bookread.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping
    public UserDTO createUser(@RequestBody UserCreateDTO userDto) {
        // Ignores client-sent userLevel: always sets 'U'
        return userService.createUser(userDto.getName(), userDto.getEmail(), userDto.getPassword());
    }

    @GetMapping("/all")
    public List<User> getUsers() {
        return userService.getUsers();
    }
}
