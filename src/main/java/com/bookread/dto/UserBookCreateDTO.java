package com.bookread.dto;

import com.bookread.model.UserBook;

public record UserBookCreateDTO(
        Integer bookId,
        UserBook.Status status,
        int currentPage,
        Boolean isFavorite
) {}