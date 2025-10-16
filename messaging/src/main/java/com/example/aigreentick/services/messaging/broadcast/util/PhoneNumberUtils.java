package com.example.aigreentick.services.messaging.broadcast.util;



import java.util.ArrayList;
import java.util.List;

public class PhoneNumberUtils {
    /**
     * Checks if a number is a valid 10-digit Indian mobile number.
     * Starts with 6, 7, 8, or 9.
     */
    public static boolean isValidIndianMobile(String number) {
        return number != null && number.matches("^[6-9]\\d{9}$");
    }

    /**
     * Validates if the mobile number has between 9 and 15 digits.
     * 
     * @param number the mobile number string (digits only)
     * @return true if valid, false otherwise
     */
    public static boolean isValidMobileNumber(String number) {
        // Ensure number is not null and consists of 9 to 15 digits
        return number != null && number.matches("^\\d{9,15}$");
    }

    /**
     * Checks if a number contains the Indian country code: +91 or 91.
     */
    public static boolean hasIndianCountryCode(String number) {
        return number != null && number.matches("^(\\+91|91)[6-9]\\d{9}$");
    }

    /**
     * Strips +91 or 91 country code from Indian number.
     */
    public static String stripIndianCountryCode(String number) {
        if (number == null)
            return null;
        return number.replaceFirst("^\\+91", "")
                .replaceFirst("^91", "");
    }

    /**
     * Normalizes the phone number to 10-digit format by removing Indian country
     * code if present.
     */
    public static String normalizeTo10Digit(String number) {
        String stripped = stripIndianCountryCode(number);
        if (isValidIndianMobile(stripped)) {
            return stripped;
        }
        return null;
    }

    /**
     * Formats a valid 10-digit Indian number to E.164 format (+91XXXXXXXXXX).
     */
    public static String formatToE164(String number) {
        if (isValidIndianMobile(number)) {
            return "+91" + number;
        }
        return number;
    }

    /**
     * Validates a number whether it's Indian (with or without code).
     */
    public static boolean isIndianNumber(String number) {
        return isValidIndianMobile(number) || hasIndianCountryCode(number);
    }

    /**
     * Validates and builds a WhatsApp-compatible phone number in E.164 format.
     * 
     * @param countryCode e.g., "91" for India, "1" for USA
     * @param phoneNumber e.g., "9876543210"
     * @return formatted number like "+919876543210"
     * @throws IllegalArgumentException if invalid
     */
    public static String buildWhatsAppNumber(String countryCode, String phoneNumber) {
        if (countryCode == null || phoneNumber == null) {
            throw new IllegalArgumentException("Country code and phone number must not be null.");
        }

        // Remove any non-digit characters
        String cleanedCountryCode = removeAnyNoneDigitCharacter(countryCode);
        String cleanedPhone = removeAnyNoneDigitCharacter(phoneNumber);

        if (cleanedCountryCode.isEmpty()) {
            throw new IllegalArgumentException("Country code must contain digits.");
        }

        if (cleanedPhone.length() < 6 || cleanedPhone.length() > 15) {
            throw new IllegalArgumentException("Phone number must be between 6 and 15 digits.");
        }

        return "+" + cleanedCountryCode + cleanedPhone;
    }

    public static String removeAnyNoneDigitCharacter(String placeholder) {
        return placeholder.replaceAll("\\D", "");
    }

    public static List<String> buildNumbersForWhatsappTemplateMessage(String countryCode, List<String> phoneNumbers) {
        String cleanedCountryCode = removeAnyNoneDigitCharacter(countryCode);
        List<String> normalizedNumbers = new ArrayList<>();

        if (phoneNumbers == null || phoneNumbers.isEmpty() || cleanedCountryCode.isEmpty()) {
            return normalizedNumbers;
        }

        for (String phoneNumber : phoneNumbers) {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                continue; // skip null or empty
            }

            // Remove all non-digit characters including +
            String cleanedNumber = removeAnyNoneDigitCharacter(phoneNumber);
            if (cleanedNumber.isEmpty()) {
                continue; // skip invalid
            }

            // Prepend country code if not already present
            if (!cleanedNumber.startsWith(cleanedCountryCode)) {
                cleanedNumber = cleanedCountryCode + cleanedNumber;
            }

            // Validate number length for WhatsApp (9–15 digits)
            if (isValidMobileNumber(cleanedNumber)) {
                normalizedNumbers.add(cleanedNumber); // No + sign
            } else {
                System.err.println("Skipping invalid number: " + phoneNumber);
            }
        }

        return normalizedNumbers;
    }
}
