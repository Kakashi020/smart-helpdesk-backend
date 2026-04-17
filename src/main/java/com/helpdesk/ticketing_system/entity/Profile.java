package com.helpdesk.ticketing_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "user_profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

    private String location;

    private String preferredLanguage = "English";

    private LocalDate joinedDate = LocalDate.now();

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference // Prevents infinite recursion in JSON
    private User user;
}