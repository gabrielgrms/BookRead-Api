package com.bookread.mapper;

import com.bookread.dto.UserBookDTO;
import com.bookread.model.UserBook;

public class UserBookMapper {
    public static UserBookDTO toDto(UserBook userBook) {
        return new UserBookDTO(userBook.getId(), userBook.getBook().getId(), userBook.getBook().getTitle(), userBook.getStatus(), userBook.getCurrentPage(), userBook.isFavorite());
    }
}