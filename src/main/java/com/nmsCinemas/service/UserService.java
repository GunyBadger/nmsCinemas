package com.nmsCinemas.service;

import com.nmsCinemas.models.User;
import com.nmsCinemas.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }

    @Transactional
    public User save(User user) {
        return repo.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}