package com.aigreentick.services.template.model;

import java.util.List;

import com.aigreentick.services.template.enums.ButtonTypes;
import com.aigreentick.services.template.enums.OtpTypes;

import lombok.*;

/**
 * Represents a button component in a WhatsApp template.
 * <p>
 * Buttons can be of different types (e.g., QUICK_REPLY, URL, PHONE_NUMBER, OTP)
 * and may
 * include optional fields like OTP type, phone number, URL, autofill text, and
 * examples.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateComponentButton {

    /**
     * Type of the button.
     * Allowed values: QUICK_REPLY, URL, PHONE_NUMBER, OTP
     */
    private ButtonTypes type;

    /**
     * Type of OTP (if button type is OTP). Can be null for other button types.
     */
    private OtpTypes otpType;

    private String phoneNumber;

    private String text;

    /**
     * Index of the button within the component (used for ordering).
     */
    private int index;

    /**
     * URL for URL type buttons. Can be null for other types.
     */
    private String url;

    /**
     * Autofill text to pre-fill in the message for QUICK_REPLY buttons.
     */
    private String autofillText;

    List<String> example;

    List<SupportedApp> supportedApps;
}
