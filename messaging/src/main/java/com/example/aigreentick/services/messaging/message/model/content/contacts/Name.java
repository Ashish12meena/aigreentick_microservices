package com.example.aigreentick.services.messaging.message.model.content.contacts;



import lombok.Data;

@Data
public class Name {
    private String formattedName;
    private String firstName;
    private String lastName;
    private String middleName;
    private String suffix;
    private String prefix;
}
