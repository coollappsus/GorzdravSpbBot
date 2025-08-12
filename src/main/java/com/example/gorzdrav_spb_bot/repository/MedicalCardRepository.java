package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.model.MedicalCard;
import com.example.gorzdrav_spb_bot.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MedicalCardRepository extends CrudRepository<MedicalCard, Long> {

    List<MedicalCard> findByOwnerUserId(Long userId);

    MedicalCard findByOwnerAndFirstNameAndLastName(User owner, String firstName, String lastName);

    Integer countByOwnerUserId(Long userId);
}
