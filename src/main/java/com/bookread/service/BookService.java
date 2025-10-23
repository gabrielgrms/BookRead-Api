package com.bookread.service;

import com.bookread.dto.BookCreateDTO;
import com.bookread.dto.BookDTO;
import com.bookread.mapper.BookMapper;
import com.bookread.model.Book;
import com.bookread.model.User;
import com.bookread.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }
}
