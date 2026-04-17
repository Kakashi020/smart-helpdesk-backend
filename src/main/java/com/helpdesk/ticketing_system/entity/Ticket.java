package com.helpdesk.ticketing_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category; // Added to match your "Hardware/Software" dropdown
    private String priority; // Predicted by Weka
    private String status = "OPEN";
    private String email;    // Used for filtering user-specific tickets
    private String classificationSource;

    @Column(columnDefinition = "TEXT")
    private String feedback; // The fix for your Controller error

    private LocalDateTime createdDate = LocalDateTime.now();

    // --- SLA / ERT LOGIC ---
    public LocalDateTime getEstimatedResolutionTime() {
        if (priority == null) return createdDate.plusDays(3);

        return switch (priority) {
            case "Critical" -> createdDate.plusHours(4);
            case "High"     -> createdDate.plusHours(8);
            case "Medium"   -> createdDate.plusHours(12);
            default         -> createdDate.plusDays(1);
        };
    }
}