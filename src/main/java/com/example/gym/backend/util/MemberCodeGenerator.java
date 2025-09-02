package com.example.gym.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public class MemberCodeGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generateMemberCode() {
        String date = LocalDateTime.now().format(formatter);
        int sequence = counter.getAndIncrement();

        if (sequence > 9999) {
            counter.set(1);
            sequence = 1;
        }

        return String.format("M%s%04d", date, sequence);
    }

    public static void resetCounter() {
        counter.set(1);
    }
}