package com.example.aigreentick.services.messaging.message.model.content.interactive;

import java.util.List;

import lombok.Data;

@Data
public class Section {
    private String title;
    private List<SectionRows> rows;
    private List<ProductItem> productItems;
}
