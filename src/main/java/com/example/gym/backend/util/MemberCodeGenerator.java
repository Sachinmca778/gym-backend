package com.example.gym.backend.util;

import com.example.gym.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MemberCodeGenerator {

    @Autowired
    private MemberRepository memberRepository;

    private static AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static String lastDate = "";

    @PostConstruct
    public void init() {
        // Initialize counter from database on startup
        initializeCounter();
    }

    private void initializeCounter() {
        String today = LocalDateTime.now().format(formatter);
        String prefix = "M" + today;
        
        try {
            // Get the highest member code for today from database
            var memberCodes = memberRepository.findMemberCodesByPrefix(prefix);
            if (memberCodes != null && !memberCodes.isEmpty()) {
                String highestCode = memberCodes.get(0);
                // Extract the sequence number from the highest code
                // Format: M202603010001 -> extract 0001
                if (highestCode.length() >= 13) {
                    String sequenceStr = highestCode.substring(9); // Skip "M" + 8 digits of date
                    try {
                        int highestSequence = Integer.parseInt(sequenceStr);
                        counter.set(highestSequence + 1);
                        lastDate = today;
                        System.out.println("MemberCodeGenerator initialized with counter: " + counter.get() + " for date: " + today);
                    } catch (NumberFormatException e) {
                        System.out.println("Could not parse sequence from: " + highestCode);
                    }
                }
            } else {
                // No members for today, start from 1
                counter.set(1);
                lastDate = today;
                System.out.println("MemberCodeGenerator initialized with counter: 1 for new day: " + today);
            }
        } catch (Exception e) {
            // If database is not available or any error, start from 1
            System.out.println("Error initializing member code generator: " + e.getMessage());
            counter.set(1);
            lastDate = today;
        }
    }

    public static synchronized String generateMemberCode() {
        String date = LocalDateTime.now().format(formatter);

        // Check if date has changed, reinitialize counter from database if needed
        if (!date.equals(lastDate)) {
            // This case is handled by the caller who will reinject the repository
            // For now, just reset locally
            counter.set(1);
            lastDate = date;
        }

        int sequence = counter.getAndIncrement();

        return String.format("M%s%04d", date, sequence);
    }

    public String generateUniqueCode() {
        String today = LocalDateTime.now().format(formatter);
        
        // If date has changed, reinitialize counter
        if (!today.equals(lastDate)) {
            initializeCounter();
        }

        // Generate code and handle potential duplicates with retry
        int maxRetries = 10;
        for (int i = 0; i < maxRetries; i++) {
            int sequence = counter.getAndIncrement();
            String memberCode = String.format("M%s%04d", today, sequence);
            
            // Check if code already exists in database
            if (!memberRepository.existsByMemberCode(memberCode)) {
                lastDate = today;
                return memberCode;
            }
            
            // Code exists, try next one (but don't exceed max retries)
            System.out.println("Member code " + memberCode + " already exists, trying next...");
        }
        
        // If we reach here after all retries, throw an exception
        throw new RuntimeException("Failed to generate unique member code after " + maxRetries + " attempts");
    }

    // Method to reinitialize counter (can be called if needed)
    public void reinitialize() {
        initializeCounter();
    }
}

