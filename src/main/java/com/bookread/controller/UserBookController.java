package com.bookread.controller;

import com.bookread.dto.*;
import com.bookread.service.BookService;
import com.bookread.service.UserBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/userbook")
public class UserBookController {
    private final UserBookService userBookService;

    @Autowired
    public UserBookController(UserBookService userBookService) {
        this.userBookService = userBookService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUserBook(@RequestBody UserBookCreateDTO userBook, Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        try {

            UserBookDTO dto = userBookService.createUserBook(
                    userEmail,userBook.bookId(),
                    userBook.isFavorite(),
                    userBook.currentPage(),
                    userBook.status()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch(RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(ex.getMessage()));
        }
    }



}
