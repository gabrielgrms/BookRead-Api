package com.bookread.controller;

import com.bookread.dto.BookCreateDTO;
import com.bookread.dto.BookDTO;
import com.bookread.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/create")
    public BookDTO createBook(@RequestBody BookCreateDTO book) {
        return bookService.createBook(book.getTitle(), book.getAuthor());
    }



}
