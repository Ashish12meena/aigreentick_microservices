package com.example.aigreentick.services.messaging.message.model.content.interactive;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class ValueDto {
    private String name;
    private String phoneNumber;
    private String inPinCode;
    private String floorNumber;
    private String buildingName;
    private String address;
    private String landmarkArea;
    private String city;
}
