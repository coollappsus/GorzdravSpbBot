package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    boolean existsByUserId(Long userId);

    User findUserByUserId(@NonNull Long id);

    @Query(value = "SELECT * FROM public.get_user_delta()", nativeQuery = true)
    List<User> getNewUsers();
}
