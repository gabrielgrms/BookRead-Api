package com.bookread.service;

import com.bookread.model.RecoverPassword;
import com.bookread.model.User;
import com.bookread.repository.RecoverPasswordRepository;
import com.bookread.repository.UserRepository;
import com.bookread.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecoverPasswordService {
    private final RecoverPasswordRepository recoverPasswordRepository;
    private final UserRepository userRepository;
    @Autowired
    public RecoverPasswordService(RecoverPasswordRepository recoverPasswordRepository,
                                  UserRepository userRepository) {
        this.recoverPasswordRepository = recoverPasswordRepository;
        this.userRepository = userRepository;
    }

    public RecoverPassword createRecoverPassword(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }

        RecoverPassword recoverPassword = recoverPasswordRepository.findByUser(user);
        if (recoverPassword == null) {
            recoverPassword = new RecoverPassword();
        }
        recoverPassword.setUser(user);
        recoverPassword.setToken(TokenUtil.generateToken());
        return recoverPasswordRepository.save(recoverPassword);
    }

    public User recoverPasswordUser(String token) {
        RecoverPassword recoverPassword = recoverPasswordRepository.findByToken(token);
        if(recoverPassword == null) return null;
        return recoverPassword.getUser();
    }

    public void deleteToken(String token){
        RecoverPassword recoverPassword = recoverPasswordRepository.findByToken(token);
        recoverPasswordRepository.delete(recoverPassword);
    }
}
