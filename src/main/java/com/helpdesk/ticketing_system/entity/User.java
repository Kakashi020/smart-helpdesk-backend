package com.helpdesk.ticketing_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.Random;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String role = "ROLE_CUSTOMER";

    // --- NEW FIELD: CUSTOMER ID ---
    @Column(unique = true)
    private String customerId;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Profile profile;

    // --- AUTOMATIC GENERATOR LOGIC ---
    @PrePersist
    public void generateId() {
        if (this.customerId == null || this.customerId.isEmpty()) {
            // Generates a random 4-digit number (1000-9999)
            int randomNum = new Random().nextInt(9000) + 1000;
            this.customerId = "CID-" + randomNum;
        }
    }
}