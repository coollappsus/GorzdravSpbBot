package com.example.gorzdrav_spb_bot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.JOIN)
    private MedicalCard medicalCard;

    private String lpuId;
    private String doctorId;

    private boolean completeStatus;
    private boolean activeStatus;

    private TimePreference timePreference;

    public void doFinished() {
        this.completeStatus = true;
        this.activeStatus = false;
    }
}
