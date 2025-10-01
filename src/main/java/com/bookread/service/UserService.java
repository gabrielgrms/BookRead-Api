package com.bookread.service;

import com.bookread.dto.UserDTO;
import com.bookread.mapper.UserMapper;
import com.bookread.model.User;
import com.bookread.repository.UserRepository;
import com.bookread.util.PasswordUtils;
import io.jsonwebtoken.security.Password;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO createUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(PasswordUtils.hashPassword(password));
        user.setUserLevel('U');
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null)
            return null;
        if(PasswordUtils.matches(password, user.getPassword()))
            return user;
        return null;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
