package com.aigreentick.services.template.model;

import java.util.List;

import com.aigreentick.services.template.enums.ComponentType;
import com.aigreentick.services.template.model.carousel.TemplateComponentCards;

import lombok.*;

/**
 * Represents a component of a WhatsApp template.
 * <p>
 * Components can include text, media, buttons, or carousel cards.
 * Each component can have optional security recommendations, OTP expiration,
 * and examples.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateComponent {

    /**
     * Type of the component.
     * Examples: HEADER, BODY, FOOTER, BUTTONS, CAROUSEL
     */
    private ComponentType type;

    /**
     * Format of the component.
     * Examples: TEXT, IMAGE, VIDEO, DOCUMENT
     */
    private String format;

    private String text;

    /**
     * URL of an image for IMAGE type components.
     */
    private String imageUrl;

    /**
     * URL of media (video or document) for MEDIA type components.
     */
    private String mediaUrl;

    /**
     * Indicates whether a security recommendation should be added for this
     * component.
     */
    private Boolean addSecurityRecommendation;

    private Integer codeExpirationMinutes;

    /**
     * List of buttons associated with this component.
     * Each button must conform to WhatsApp template rules.
     */
    private List<TemplateComponentButton> buttons;

    /**
     * List of carousel cards associated with this component.
     * Only applicable if the component type is CAROUSEL.
     */
    private List<TemplateComponentCards> cards;

    private TemplateExample example;

}
