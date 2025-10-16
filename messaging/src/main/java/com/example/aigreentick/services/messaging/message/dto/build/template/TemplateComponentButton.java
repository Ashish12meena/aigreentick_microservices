package com.example.aigreentick.services.messaging.message.dto.build.template;
import java.util.List;
import lombok.Data;

@Data
public class TemplateComponentButton {
    private String type; // QUICK_REPLY, URL, PHONE_NUMBER, OTP
    private String otpType;
    private String phoneNumber;
    private String text;
    private int index;
    private String url;
    private String autofillText;
    List<String> example;
    List<SupportedApp> supportedApps;
}
