package com.bookread.dto;

import com.bookread.model.UserBook;
import com.bookread.model.UserBookId;

public record UserBookDTO(
        UserBookId id,
        Integer bookId,
        String bookTitle,
        UserBook.Status status,
        Integer currentPage,
        Boolean isFavorite
) {}
