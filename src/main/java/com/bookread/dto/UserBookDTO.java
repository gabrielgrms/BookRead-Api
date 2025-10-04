package com.bookread.dto;

import com.bookread.model.UserBook;

public record UserBookDTO(
        Integer id,
        Integer bookId,
        String bookTitle,
        UserBook.Status status,
        Integer currentPage,
        Boolean isFavorite
) {}
