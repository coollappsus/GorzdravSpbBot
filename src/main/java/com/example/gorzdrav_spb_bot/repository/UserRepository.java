package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.model.User;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByUserId(Long userId);

    User findUserByUserId(@NonNull Long id);
}
