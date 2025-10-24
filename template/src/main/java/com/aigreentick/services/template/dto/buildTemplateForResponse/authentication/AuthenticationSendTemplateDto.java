package com.aigreentick.services.template.dto.buildTemplateForResponse.authentication;
import com.aigreentick.services.template.dto.buildTemplateForResponse.BaseSendTemplate;

import lombok.Data;


@Data
public class AuthenticationSendTemplateDto implements BaseSendTemplate {
    private String messagingProduct = "whatsapp";
    private String to;
    private String recipientType = "individual";
    private String type = "template";
    private AuthenticationTemplateDto template;
}
