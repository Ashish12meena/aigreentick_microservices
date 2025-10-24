package com.aigreentick.services.template.model;

import java.util.List;

import lombok.Data;

@Data
public class TemplateExample {
    List<String> headerText; // for TEXT format
    List<String> headerHandle;
    List<List<String>> bodyText;
}
