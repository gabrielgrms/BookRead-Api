package com.bookread.repository;

import com.bookread.model.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {}
