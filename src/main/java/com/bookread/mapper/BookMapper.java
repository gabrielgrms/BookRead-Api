package com.bookread.mapper;

import com.bookread.dto.BookDTO;
import com.bookread.model.Book;

public class BookMapper {
    public static BookDTO toDto(Book book) {
        return new BookDTO(book.getId(), book.getTitle(), book.getAuthor());
    }
}