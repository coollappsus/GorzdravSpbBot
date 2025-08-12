package com.example.gorzdrav_spb_bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private User owner;

    private String firstName;
    private String lastName;
    private String MiddleName;

    private Date birthDate;

    private int lpuId;

    private String patientId;
}
