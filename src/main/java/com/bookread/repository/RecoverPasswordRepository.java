package com.bookread.repository;

import com.bookread.model.RecoverPassword;
import com.bookread.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecoverPasswordRepository extends JpaRepository<RecoverPassword, Integer> {
    RecoverPassword findByUser(User user);
    RecoverPassword findByToken(String token);
}
