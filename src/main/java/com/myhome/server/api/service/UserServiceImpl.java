package com.myhome.server.api.service;

import com.myhome.server.api.dto.UserDto;
import com.myhome.server.db.entity.UserEntity;
import com.myhome.server.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    @Autowired
    UserRepository repository;

    @Override
    public UserEntity findByUserId(long userId) {
        UserEntity entity = repository.findByUserId(userId);
        return entity;
    }

    @Override
    public Optional<UserEntity> findById(String id) {
        Optional<UserEntity> entity = repository.findById(id);
        return entity;
    }

    @Override
    public int updateUser(UserDto userDto) {
        UserEntity entity = repository.save(new UserEntity(userDto));
        System.out.println(entity.toString());
        return 1;
    }

    @Override
    public void updateTokens(String accessToken, String refreshToken, String Id) {
        repository.updateTokens(accessToken, refreshToken, Id);
    }


    @Transactional
    @Override
    public boolean checkPassword(String inputPassword, String id) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(inputPassword, loadUserByUsername(id).getPassword());
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        System.out.println("loadUserByUserName : " + repository.findById(id));
        return repository.findById(id).orElseThrow(()->new UsernameNotFoundException(id));
    }
}
