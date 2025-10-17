package com.bookread.repository;

import com.bookread.model.UserBook;
import com.bookread.model.UserBookId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, UserBookId> {}
