package com.helpdesk.ticketing_system.controller;

import com.helpdesk.ticketing_system.entity.Ticket;
import com.helpdesk.ticketing_system.service.TicketService;
import com.helpdesk.ticketing_system.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "https://smarthelpdeskticketingsolution.netlify.app")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    // --- 1. CORE CRUD ---
    @PostMapping
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        return ResponseEntity.ok(ticketService.createTicket(ticket));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    // --- 2. THE HANDSHAKE PROTOCOL ---

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<?> proposeResolve(@PathVariable Long id) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus("PENDING_CONFIRMATION");
            ticketRepository.save(ticket);
            return ResponseEntity.ok(ticket);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/confirm-resolve")
    public ResponseEntity<?> confirmResolve(@PathVariable Long id) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus("RESOLVED");
            ticketRepository.save(ticket);
            return ResponseEntity.ok(ticket);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/reject-resolve")
    public ResponseEntity<?> rejectResolve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus("OPEN");
            // Save the user's feedback so the Admin can read it in the table
            ticket.setFeedback(body.get("feedback"));
            ticketRepository.save(ticket);
            return ResponseEntity.ok(ticket);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- 3. REAL-TIME WEKA ANALYTICS ---

    @GetMapping("/weka-stats")
    public ResponseEntity<?> getWekaStats() {
        List<Ticket> allTickets = ticketRepository.findAll();

        long total = allTickets.size();
        long critical = allTickets.stream().filter(t -> "Critical".equals(t.getPriority())).count();
        long high = allTickets.stream().filter(t -> "High".equals(t.getPriority())).count();
        long medium = allTickets.stream().filter(t -> "Medium".equals(t.getPriority())).count();
        long low = allTickets.stream().filter(t -> "Low".equals(t.getPriority())).count();
        long resolved = allTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();

        // Logic: Accuracy is calculated by successful resolutions vs total attempts
        double accuracy = total > 0 ? ((double) resolved / total) * 100 : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("totalTickets", total);
        response.put("criticalCount", critical);
        response.put("accuracyRate", Math.round(accuracy * 100.0) / 100.0);

        Map<String, Long> distribution = new HashMap<>();
        distribution.put("Critical", critical);
        distribution.put("High", high);
        distribution.put("Medium", medium);
        distribution.put("Low", low);
        response.put("distribution", distribution);

        return ResponseEntity.ok(response);
    }
}