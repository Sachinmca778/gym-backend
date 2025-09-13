package com.example.gym.backend.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MemberCodeGenerator {

    private static AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static String lastDate = "";

    public static synchronized String generateMemberCode() {
        String date = LocalDateTime.now().format(formatter);

        if (!date.equals(lastDate)) {
            counter.set(1);
            lastDate = date;
        }

        int sequence = counter.getAndIncrement();

        return String.format("M%s%04d", date, sequence);
    }

    public static String generateUniqueCode() {
        return generateMemberCode();
    }
}
