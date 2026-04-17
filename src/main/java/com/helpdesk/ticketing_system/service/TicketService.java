package com.helpdesk.ticketing_system.service;

import com.helpdesk.ticketing_system.entity.Ticket;
import com.helpdesk.ticketing_system.repository.TicketRepository;
import com.helpdesk.ticketing_system.util.WekaModelTrainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.tokenizers.NGramTokenizer;
import java.util.*;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    private NaiveBayesMultinomialText classifier;
    private Instances dataStructure;

    // --- LAYER 1: THE INDEPENDENT GUARD (Hard Rules) ---
    private static final Map<String, String> HARD_RULES = new LinkedHashMap<>();
    static {
        // 10 CRITICAL RULES
        HARD_RULES.put("hacked account", "Critical");
        HARD_RULES.put("unauthorized transaction", "Critical");
        HARD_RULES.put("money deducted order failed", "Critical");
        HARD_RULES.put("stolen credit card", "Critical");
        HARD_RULES.put("received empty box", "Critical");
        HARD_RULES.put("cannot access account", "Critical");
        HARD_RULES.put("personal data leak", "Critical");
        HARD_RULES.put("fraudulent activity", "Critical");
        HARD_RULES.put("payment stuck", "Critical");
        HARD_RULES.put("scam company", "Critical");

        // 10 HIGH RULES
        HARD_RULES.put("double charged", "High");
        HARD_RULES.put("refund not received", "High");
        HARD_RULES.put("wrong item delivered", "High");
        HARD_RULES.put("damaged on arrival", "High");
        HARD_RULES.put("return pickup failed", "High");
        HARD_RULES.put("promo code not working", "High");
        HARD_RULES.put("invoice missing", "High");
        HARD_RULES.put("arrived broken", "High");
        HARD_RULES.put("delivered marked but not here", "High");
        HARD_RULES.put("incorrect billing", "High");

        // 10 MEDIUM RULES
        HARD_RULES.put("delivery delayed", "Medium");
        HARD_RULES.put("tracking not updating", "Medium");
        HARD_RULES.put("change shipping address", "Medium");
        HARD_RULES.put("cancel order", "Medium");
        HARD_RULES.put("subscription renewal", "Medium");
        HARD_RULES.put("gift card not", "Medium");
        HARD_RULES.put("membership benefits", "Medium");
        HARD_RULES.put("bulk order", "Medium");
        HARD_RULES.put("size guide", "Medium");
        HARD_RULES.put("out of stock", "Medium");
    }

    @PostConstruct
    public void initAI() {
        try {
            System.out.println("🤖 Initializing Hybrid Layered AI...");
            dataStructure = WekaModelTrainer.getTrainingData();

            classifier = new NaiveBayesMultinomialText();

            NGramTokenizer tokenizer = new NGramTokenizer();
            tokenizer.setNGramMinSize(1);
            tokenizer.setNGramMaxSize(3);
            tokenizer.setDelimiters("\\W");

            classifier.setTokenizer(tokenizer);
            classifier.setLowercaseTokens(true);
            classifier.setUseWordFrequencies(true);

            classifier.buildClassifier(dataStructure);
            System.out.println("✅ AI Model trained with " + dataStructure.numInstances() + " N-Gram patterns.");
        } catch (Exception e) {
            System.err.println("❌ AI Load Failed: " + e.getMessage());
        }
    }

    public Ticket createTicket(Ticket ticket) {
        // Combine text for better context analysis
        String fullText = (ticket.getSubject() + " " + ticket.getDescription()).toLowerCase();

        // STEP 1: Check Layer 1 (Independent Hard Rules)
        String priority = checkHardRules(fullText);
        String category = "General";

        if (priority != null) {
            // Found a direct rule match
            ticket.setPriority(priority);
            ticket.setClassificationSource("Rule Match");
            System.out.println("🛡️ Layer 1 Guard triggered for: " + priority);
        }
        else {
            // STEP 2: Fallback to Layer 2 (Weka N-Gram AI)
            String prediction = askAI(fullText);
            ticket.setClassificationSource("AI Prediction");

            if (prediction.contains("_")) {
                String[] parts = prediction.split("_");
                priority = parts[0];
                category = parts[1];
            } else {
                priority = prediction;
            }

            ticket.setPriority(priority);
            ticket.setCategory(category);
            System.out.println("🧠 Layer 2 AI Predicted: " + priority + " (" + category + ")");
        }

        if (ticket.getStatus() == null || ticket.getStatus().isEmpty()) {
            ticket.setStatus("OPEN");
        }

        return ticketRepository.save(ticket);
    }

    private String checkHardRules(String text) {
        if (text == null) return null;
        for (Map.Entry<String, String> rule : HARD_RULES.entrySet()) {
            if (text.contains(rule.getKey())) return rule.getValue();
        }
        return null;
    }

    private String askAI(String text) {
        try {
            if (text == null || text.trim().isEmpty()) return "Low_Software";

            DenseInstance instance = new DenseInstance(2);
            instance.setDataset(dataStructure);
            instance.setValue(dataStructure.attribute(0), text);
            instance.setMissing(1);

            double predictedIndex = classifier.classifyInstance(instance);
            return dataStructure.classAttribute().value((int) predictedIndex);
        } catch (Exception e) {
            System.err.println("AI Error: " + e.getMessage());
            return "Medium_Software";
        }
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }
}