package com.example.gorzdrav_spb_bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tg_user_name")
    private String userName;

    @Column(name = "tg_user_id")
    private Long userId;

    @Column(name = "tg_chat_id")
    private Long chatId;

    @OneToMany(mappedBy = "owner")
    private List<MedicalCard> medicalCards;
}
