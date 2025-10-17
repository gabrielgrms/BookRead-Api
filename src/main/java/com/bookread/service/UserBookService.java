package com.bookread.service;

import com.bookread.dto.UserBookDTO;
import com.bookread.mapper.UserBookMapper;
import com.bookread.model.Book;
import com.bookread.model.User;
import com.bookread.model.UserBook;
import com.bookread.model.UserBookId;
import com.bookread.repository.BookRepository;
import com.bookread.repository.UserBookRepository;
import com.bookread.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserBookService {
    private final UserBookRepository userBookRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;

    @Autowired
    public UserBookService(UserBookRepository userBookRepo, UserRepository userRepo, BookRepository bookRepo) {
        this.userBookRepo = userBookRepo;
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
    }

    public UserBookDTO createUserBook(String userEmail, Integer bookId, Boolean isFavorite, Integer currentPage, UserBook.Status status) {
        User user = userRepo.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        UserBookId userBookId = new UserBookId(user.getId(), bookId);

        UserBook userBook = new UserBook();
        userBook.setId(userBookId);
        userBook.setUser(user); // sets the user object, which includes userId
        userBook.setBook(book);
        userBook.setStatus(status);
        userBook.setCurrentPage(currentPage);
        userBook.setFavorite(isFavorite);
        UserBook savedUserBook = userBookRepo.save(userBook);
        return UserBookMapper.toDto(savedUserBook);
    }
}
