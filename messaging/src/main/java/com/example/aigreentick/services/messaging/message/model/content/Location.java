package com.example.aigreentick.services.messaging.message.model.content;

import lombok.Data;

@Data
public class Location {
  private String latitude;
  private String longitude;
  private String name;
  private String address;
}
