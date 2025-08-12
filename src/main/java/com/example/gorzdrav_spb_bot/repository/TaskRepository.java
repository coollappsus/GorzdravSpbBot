package com.example.gorzdrav_spb_bot.repository;

import com.example.gorzdrav_spb_bot.model.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {

    List<Task> findByCompleteStatusAndActiveStatus(Boolean completeStatus, Boolean activeStatus);
}
