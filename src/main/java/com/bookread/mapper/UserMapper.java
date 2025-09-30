package com.bookread.mapper;

import com.bookread.model.User;
import com.bookread.dto.UserDTO;

public class UserMapper {
    public static UserDTO toDto(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}