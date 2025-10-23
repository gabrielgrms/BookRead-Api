package com.bookread.controller;

import com.bookread.dto.ApiResponse;
import com.bookread.dto.BookCreateDTO;
import com.bookread.dto.BookDTO;
import com.bookread.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBook(@RequestBody BookCreateDTO book) {
        try{
            BookDTO bookDto = bookService.createBook(book.title(), book.author());
            return ResponseEntity.status(HttpStatus.CREATED).body(bookDto);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getBooks() {
        return ResponseEntity.ok(bookService.getBooks());
    }

}
