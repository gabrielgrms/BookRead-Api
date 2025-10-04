package com.bookread.controller;

import com.bookread.dto.BookCreateDTO;
import com.bookread.dto.BookDTO;
import com.bookread.dto.UserBookCreateDTO;
import com.bookread.dto.UserBookDTO;
import com.bookread.service.BookService;
import com.bookread.service.UserBookService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public UserBookDTO createUserBook(@RequestBody UserBookCreateDTO userBook, Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        return userBookService.createUserBook(userEmail,userBook.bookId(), userBook.isFavorite(), userBook.currentPage(), userBook.status());
    }



}
