package com.example.aigreentick.services.messaging.message.model.content.contacts;


import lombok.Data;

@Data
public class Address {
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String countryCode;
    private String type;
}
