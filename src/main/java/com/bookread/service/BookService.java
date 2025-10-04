package com.bookread.service;

import com.bookread.dto.BookCreateDTO;
import com.bookread.dto.BookDTO;
import com.bookread.mapper.BookMapper;
import com.bookread.model.Book;
import com.bookread.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookService {
    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookDTO createBook(String title, String author) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        Book savedBook = bookRepository.save(book);
        return BookMapper.toDto(savedBook);
    }
}
