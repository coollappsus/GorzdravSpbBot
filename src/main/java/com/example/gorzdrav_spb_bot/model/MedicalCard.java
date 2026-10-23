package com.example.gorzdrav_spb_bot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Date;

@Entity
@Table(name = "medical_card")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    @ToString.Exclude
    @JoinColumn(name = "owner_id")
    private User owner;

    private String firstName;
    private String lastName;
    private String middleName;

    private Date birthDate;

    private int lpuId;

    private String patientId;
}
