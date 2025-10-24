package com.aigreentick.services.template.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportedApp {
    private String packageName;
    private String signatureHash;
}
