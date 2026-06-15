package com.ahsmart.campusmarket.service.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock ToyyibPay gateway — simulates real payment processing without moving real money.
 * Uses special test card numbers to trigger different outcomes (SUCCESS, DECLINED, etc.).
 * Swap this with a real ToyyibPay adapter later to go live.
 */
@Service
public class MockToyyibPayService {

    // Special test card numbers that trigger specific outcomes.
    private static final String CARD_SUCCESS       = "4242424242424242";
    private static final String CARD_DECLINED      = "4000000000000002";
    private static final String CARD_INSUFFICIENT  = "4000000000009995";

    // Validates card details just like a real gateway would.
    public Map<String, Object> validateCard(String cardNumber, String expiryMonth,
                                             String expiryYear, String cvv, String cardHolder) {
        Map<String, Object> result = new HashMap<>();

        // Check cardholder name is provided.
        if (cardHolder == null || cardHolder.trim().isEmpty()) {
            result.put("valid", false);
            result.put("error", "Cardholder name is required.");
            return result;
        }

        // Strip ALL non-digit characters from card number (spaces, dashes, any encoding quirks).
        String cleanCard = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");

        // Card number must be exactly 16 digits (all numeric). No Luhn checksum —
        // this is a mock gateway, so any reasonable 16-digit card is accepted.
        if (cleanCard.length() != 16 || !cleanCard.matches("\\d+")) {
            result.put("valid", false);
            result.put("error", "Card number must be 16 digits.");
            return result;
        }

        // Validate expiry month (01-12).
        int month;
        try {
            month = Integer.parseInt(expiryMonth);
            if (month < 1 || month > 12) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            result.put("valid", false);
            result.put("error", "Invalid expiry month.");
            return result;
        }

        // Validate expiry year and check card is not expired.
        int year;
        try {
            year = Integer.parseInt(expiryYear);
            // Support both 2-digit (26) and 4-digit (2026) year input.
            if (year < 100) year += 2000;
            LocalDateTime now = LocalDateTime.now();
            if (year < now.getYear() || (year == now.getYear() && month < now.getMonthValue())) {
                result.put("valid", false);
                result.put("error", "Card has expired.");
                return result;
            }
        } catch (NumberFormatException e) {
            result.put("valid", false);
            result.put("error", "Invalid expiry year.");
            return result;
        }

        // CVV must be 3 or 4 digits.
        String cleanCvv = cvv == null ? "" : cvv.trim();
        if (!cleanCvv.matches("\\d{3,4}")) {
            result.put("valid", false);
            result.put("error", "CVV must be 3 or 4 digits.");
            return result;
        }

        result.put("valid", true);
        result.put("cleanCard", cleanCard);
        return result;
    }

    // Processes a mock payment — returns a result map with status, reference, and message.
    public Map<String, Object> processPayment(String cleanCardNumber, BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();

        // Generate a unique transaction reference (like a real gateway would).
        String txnRef = generateTransactionReference();
        result.put("reference", txnRef);
        result.put("amount", amount);

        // Check for special test card numbers that simulate failures.
        switch (cleanCardNumber) {
            case CARD_DECLINED:
                result.put("success", false);
                result.put("message", "Card declined by issuing bank.");
                result.put("code", "DECLINED");
                return result;
            case CARD_INSUFFICIENT:
                result.put("success", false);
                result.put("message", "Insufficient funds on card.");
                result.put("code", "INSUFFICIENT_FUNDS");
                return result;
            default:
                // Any other valid card succeeds.
                result.put("success", true);
                result.put("message", "Payment processed successfully.");
                result.put("code", "SUCCESS");
                return result;
        }
    }

    // Generates a realistic-looking transaction reference.
    private String generateTransactionReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TBP-" + datePart + "-" + uniquePart;
    }
}

