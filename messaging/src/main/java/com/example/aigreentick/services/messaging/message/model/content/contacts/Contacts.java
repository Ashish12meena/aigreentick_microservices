package com.example.aigreentick.services.messaging.message.model.content.contacts;

import java.util.List;

import lombok.Data;

@Data
public class Contacts {
    List<Address> addresses;
    private String birthday;
    List<Email> emails;
    Name name;
    Org org;
    List<Phone> phones;
    List<Url> urls;
}
