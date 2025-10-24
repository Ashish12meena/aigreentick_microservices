package com.example.aigreentick.services.messaging.message.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.aigreentick.services.common.util.JsonUtils;
import com.example.aigreentick.services.messaging.broadcast.dto.request.BuildTemplateRequestDto;
import com.example.aigreentick.services.messaging.message.client.dto.PhoneBookResponseDto;
import com.example.aigreentick.services.messaging.message.client.service.impl.FileClient;
import com.example.aigreentick.services.messaging.message.client.service.impl.PhoneBookEntryCleint;
import com.example.aigreentick.services.messaging.message.constants.MessageConstants;
import com.example.aigreentick.services.messaging.message.dto.build.template.Template;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateCarouselButton;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateCarouselCardComponent;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateComponent;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateComponentButton;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateComponentCards;
import com.example.aigreentick.services.messaging.message.dto.build.template.TemplateText;
import com.example.aigreentick.services.messaging.message.dto.interfaces.MediaParameter;
import com.example.aigreentick.services.messaging.message.dto.interfaces.TextParameter;
import com.example.aigreentick.services.messaging.message.dto.request.MessageRequest;
import com.example.aigreentick.services.messaging.message.enums.ButtonTypes;
import com.example.aigreentick.services.messaging.message.enums.ComponentType;
import com.example.aigreentick.services.messaging.message.enums.MediaType;
import com.example.aigreentick.services.messaging.message.enums.MessageType;
import com.example.aigreentick.services.messaging.message.enums.TemplateCategory;
import com.example.aigreentick.services.messaging.message.exceptions.CarouselConfigurationException;
import com.example.aigreentick.services.messaging.message.exceptions.InvalidMediaType;
import com.example.aigreentick.services.messaging.message.exceptions.InvalidTemplateCategory;
import com.example.aigreentick.services.messaging.message.exceptions.InvalidTemplateComponentType;
import com.example.aigreentick.services.messaging.message.model.content.media.Document;
import com.example.aigreentick.services.messaging.message.model.content.media.Image;
import com.example.aigreentick.services.messaging.message.model.content.media.Media;
import com.example.aigreentick.services.messaging.message.model.content.media.Video;
import com.example.aigreentick.services.messaging.message.model.content.parameters.Language;
import com.example.aigreentick.services.messaging.message.model.content.parameters.Parameter;
import com.example.aigreentick.services.messaging.message.model.content.parameters.Product;
import com.example.aigreentick.services.messaging.message.model.content.template.Card;
import com.example.aigreentick.services.messaging.message.model.content.template.CarouselComponent;
import com.example.aigreentick.services.messaging.message.model.content.template.Component;
import com.example.aigreentick.services.messaging.message.model.content.template.LimitedTimeOffer;
import com.example.aigreentick.services.messaging.message.model.content.template.SendableTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * This service file used to build authentication , marketing and utility template that are used to send message on whatsapp
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageTemplateBuilderServiceImpl {
    private final PhoneBookEntryCleint phoneBookEntryService;
    private final FileClient mediaService;

    private static final int MAX_CARDS = MessageConstants.MAX_CARDS;
    private static final int MAX_BUTTONS_PER_CARD = MessageConstants.MAX_BUTTONS_PER_CARD;

    public List<MessageRequest> buildSendableTemplates(
            Long userId,
            List<String> phoneNumbers,
            Template template,
            BuildTemplateRequestDto requestDto) {
        String defaultValue = Optional.ofNullable(requestDto.getDefaultValue()).orElse("default");
        String mediaId = requestDto.isMedia() ? mediaService.getMediaIdById(requestDto.getMediaId()) : null;
        TemplateCategory templateCategory = TemplateCategory.valueOf(template.getCategory());

        return switch (templateCategory) {
            case AUTHENTICATION ->
                buildAuthenticationSendTemplates(phoneNumbers, template, requestDto);

            case MARKETING, UTILITY -> {
                Map<String, Map<String, String>> parameters = buildParameters(
                        requestDto, phoneNumbers, template, userId, defaultValue).getData();
                yield buildMarketingSendTemplates(phoneNumbers, template, parameters, requestDto,
                        mediaId);
            }
            default -> throw new InvalidTemplateCategory("Unsupported template category: " + templateCategory);
        };
    }

    // ======================AUTHENTICATION================================================================================

    private List<MessageRequest> buildAuthenticationSendTemplates(
            List<String> phoneNumbers,
            Template template,
            BuildTemplateRequestDto requestDto) {
        return phoneNumbers.stream()
                .map(number -> buildAuthenticationSendTemplate(number, template, requestDto))
                .collect(Collectors.toList());
    }

    public MessageRequest buildAuthenticationSendTemplate(
            String phoneNumber,
            Template template,
            BuildTemplateRequestDto requestDto) {

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setTo(phoneNumber);

        SendableTemplate sendableTemplate = new SendableTemplate();
        sendableTemplate.setName(template.getName());
        sendableTemplate.setLanguage(new Language(requestDto.getLanguageCode()));

        List<Component> components = new ArrayList<>();

        Component bodyComponent = new Component();
        bodyComponent.setType(ComponentType.BODY.getValue().toLowerCase());
        Parameter parameter = new Parameter();
        parameter.setType("text");
        parameter.setText(requestDto.getOtp());
        bodyComponent.setParameters(List.of(parameter));
        components.add(bodyComponent);

        Component buttonComponent = new Component();
        buttonComponent.setType("button");
        buttonComponent.setSubType(ButtonTypes.URL.getValue().toLowerCase());
        buttonComponent.setIndex("0");

        Parameter buttonParameter = new Parameter();
        buttonParameter.setType("text");
        buttonParameter.setText(requestDto.getOtp());
        buttonComponent.setParameters(List.of(buttonParameter));
        components.add(buttonComponent);

        sendableTemplate.setComponents(components);

        messageRequest.setTemplate(sendableTemplate);

        return messageRequest;
    }

    // ======================MARKETING=============================================================

    private List<MessageRequest> buildMarketingSendTemplates(
            List<String> phoneNumbers,
            Template template,
            Map<String, Map<String, String>> variables,
            BuildTemplateRequestDto buildTemplateRequestDto,
            String mediaId) {

        return phoneNumbers.stream()
                .map(phoneNumber -> buildMarketingSendTemplate(phoneNumber, template, variables.get(phoneNumber),
                        buildTemplateRequestDto, mediaId))
                .toList();
    }

    public MessageRequest buildMarketingSendTemplate(
            String phoneNumber,
            Template template,
            Map<String, String> variables,
            BuildTemplateRequestDto requestDto,
            String mediaId) {

        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setTo(phoneNumber);
        messageRequest.setType(MessageType.TEMPLATE);

        SendableTemplate sendableTemplate = new SendableTemplate();
        sendableTemplate.setName(template.getName());
        sendableTemplate.setLanguage(new Language(template.getLanguage()));

        List<Component> components = new ArrayList<>();

        for (TemplateComponent comp : template.getComponents()) {
            switch (ComponentType.fromValue(comp.getType())) {
                case HEADER -> {
                    Component header = buildHeaderComponent(comp, mediaId, template, variables);
                    addIfNotNull(components, header);
                }
                case BODY -> {
                    Component body = buildBodyComponent(template, variables);
                    addIfNotNull(components, body);
                }
                case BUTTONS -> {
                    List<Component> buttonComponents = buildButtonComponents(template, comp, variables);
                    addAllIfNotEmpty(components, buttonComponents);
                }
                case LIMITED_TIME_OFFER -> {
                    Component lto = buildLimitedTimeOfferComponent(requestDto);
                    addIfNotNull(components, lto);
                }
                case CAROUSEL -> {
                    Component carousel = buildCarouselComponent(comp, template, variables, requestDto);
                    addIfNotNull(components, carousel);
                }
                default -> throw new InvalidTemplateComponentType("Unsupported component type: " + comp.getType());
            }
        }

        addCopyCodeButtonIfPresent(requestDto, template, components);

        sendableTemplate.setComponents(components);
        messageRequest.setTemplate(sendableTemplate);

        // debug log of payload
        try {
            log.debug("Built marketing payload for {}: {}", phoneNumber,
                    JsonUtils.serializeToString(messageRequest));
        } catch (Exception e) {
            log.debug("Could not serialize payload for debug logging", e);
        }

        return messageRequest;

    }

    // ============Build-Components-HEADER-BODY-BUTTONS-COUPON-CAROUSEL-LTO=================================================================================

    private Component buildCarouselComponent(
            TemplateComponent comp,
            Template template,
            Map<String, String> variables,
            BuildTemplateRequestDto requestDto) {
        if (comp.getCards() == null || comp.getCards().isEmpty()) {
            throw new CarouselConfigurationException("Carousel component must contain at least one card");
        }
        if (comp.getCards().size() > MAX_CARDS) {
            throw new CarouselConfigurationException("Carousel supports max " + MAX_CARDS + " cards");
        }

        Queue<String> productRetailerIds = new ConcurrentLinkedQueue<>(requestDto.getProductRetailerIds());
        Queue<String> mediaIds = new ConcurrentLinkedQueue<>(
                Optional.ofNullable(requestDto.getMediaIdsForCarosel()).orElse(Collections.emptyList()));

        Component component = new Component();
        component.setType(comp.getType().toLowerCase()); // carousel

        List<Card> cards = comp.getCards().stream()
                .map(templateCard -> buildCard(templateCard, comp.getFormat(), template, variables, mediaIds,
                        productRetailerIds, requestDto))
                .toList();

        component.setCards(cards);
        return component;
    }

    private Card buildCard(
            TemplateComponentCards templateCard,
            String format,
            Template template,
            Map<String, String> variables,
            Queue<String> mediaQueue,
            Queue<String> productQueue,
            BuildTemplateRequestDto requestDto) {

        Card card = new Card();
        card.setCardIndex(templateCard.getIndex());

        List<CarouselComponent> carouselComponents = new ArrayList<>();

        for (TemplateCarouselCardComponent comp : templateCard.getComponents()) {
            List<CarouselComponent> builtComponents = buildComponentsOfCarousel(comp, format, template, variables,
                    mediaQueue, productQueue, requestDto);
            if (builtComponents != null && !builtComponents.isEmpty()) {
                carouselComponents.addAll(builtComponents);
            }
        }

        card.setComponents(carouselComponents);
        return card;
    }

    private List<CarouselComponent> buildComponentsOfCarousel(
            TemplateCarouselCardComponent templateCarouselComponent,
            String format,
            Template template,
            Map<String, String> variables,
            Queue<String> mediaQueue,
            Queue<String> productQueue,
            BuildTemplateRequestDto requestDto) {

        switch (ComponentType.fromValue(templateCarouselComponent.getType())) {
            case HEADER -> {
                return List.of(
                        MediaType.fromValue(templateCarouselComponent.getFormat()) == MediaType.PRODUCT
                                ? buildHeaderProductCarouselComponent(templateCarouselComponent,
                                        templateCarouselComponent.getFormat(),
                                        template, requestDto, productQueue.poll())
                                : buildHeaderMediaCarouselComponent(templateCarouselComponent,
                                        templateCarouselComponent.getFormat(),
                                        template, requestDto, mediaQueue.poll()));
            }
            case BODY -> {
                return List.of(buildBodyCarouselComponent(template, variables));
            }
            case BUTTONS -> {
                return buildButtonCarouselComponents(template, templateCarouselComponent, variables);
            }
            default -> throw new InvalidTemplateComponentType(
                    "Unsupported component type: " + templateCarouselComponent.getType());
        }
    }

    private List<CarouselComponent> buildButtonCarouselComponents(Template template,
            TemplateCarouselCardComponent templateCarouselComponent, Map<String, String> variables) {
        List<TemplateCarouselButton> buttons = Optional.ofNullable(templateCarouselComponent.getButtons())
                .orElse(Collections.emptyList());

        String payload = "payload";

        if (buttons.size() > MAX_BUTTONS_PER_CARD) {
            throw new CarouselConfigurationException(
                    "Carousel card supports at most " + MAX_BUTTONS_PER_CARD + " buttons");
        }
        List<CarouselComponent> buttonComponents = new ArrayList<>();

        for (TemplateCarouselButton button : templateCarouselComponent.getButtons()) {
            switch (ButtonTypes.fromValue(button.getType())) {
                case URL -> {
                    Parameter param = template.getTexts().stream()
                            .filter(t -> "BUTTON".equalsIgnoreCase(t.getType()))
                            .map(t -> buildCarouselTextParam(variables, t.getText()))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    if (param != null) {
                        CarouselComponent component = new CarouselComponent();
                        component.setType("button");
                        component.setSubType(button.getType().toLowerCase()); // url
                        component.setIndex(button.getIndex()); // button position
                        component.setParameters(List.of(param));
                        buttonComponents.add(component);
                    } else {
                        log.debug(
                                "Skipping URL carousel button at index {} because runtime BUTTON parameter not provided",
                                button.getIndex());
                    }
                }
                case QUICK_REPLY -> {
                    Parameter param = template.getTexts().stream()
                            .filter(t -> "BUTTON".equalsIgnoreCase(t.getType()))
                            .map(t -> buildCarouselQuickReplyParam(payload))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    if (param != null) {
                        CarouselComponent component = new CarouselComponent();
                        component.setType("button");
                        component.setSubType(button.getType().toLowerCase()); // quick_reply
                        component.setIndex(button.getIndex());
                        component.setParameters(List.of(param));
                        buttonComponents.add(component);
                    } else {
                        log.debug(
                                "Skipping QUICK_REPLY carousel button at index {} because runtime BUTTON parameter not provided",
                                button.getIndex());
                    }
                }
            }
        }

        return buttonComponents.isEmpty() ? null : buttonComponents;
    }

    private CarouselComponent buildBodyCarouselComponent(Template template, Map<String, String> variables) {
        List<TemplateText> bodyTexts = template.getTexts().stream()
                .filter(t -> ComponentType.BODY.getValue().equalsIgnoreCase(t.getType()))
                .toList();

        List<Parameter> params = bodyTexts.stream()
                .map(t -> variables.get(t.getText()))
                .filter(Objects::nonNull)
                .map(val -> buildTextParameter(val, Parameter::new))
                .toList();

        if (params.isEmpty())
            return null;

        CarouselComponent component = new CarouselComponent();
        component.setType(ComponentType.BODY.getValue().toLowerCase());
        component.setParameters(params);
        return component;
    }

    private CarouselComponent buildHeaderMediaCarouselComponent(TemplateCarouselCardComponent templateCarouselComponent,
            String format, Template template, BuildTemplateRequestDto requestDto, String mediaId) {
        CarouselComponent headerDto = new CarouselComponent();
        headerDto.setType(ComponentType.HEADER.getValue().toLowerCase());
        MediaType mediaType = MediaType.fromValue(format);
        List<Parameter> carouselParameterDto = new ArrayList<>();
        carouselParameterDto.add(buildMediaParameter(mediaType, mediaId, Parameter::new));
        headerDto.setParameters(carouselParameterDto);
        return headerDto;
    }

    private CarouselComponent buildHeaderProductCarouselComponent(
            TemplateCarouselCardComponent templateCarouselComponent, String format, Template template,
            BuildTemplateRequestDto requestDto, String productRetailerId) {
        CarouselComponent headerDto = new CarouselComponent();
        headerDto.setType(ComponentType.HEADER.getValue().toLowerCase());
        List<Parameter> carouselParameterDto = new ArrayList<>();
        carouselParameterDto.add(buildProductParameter(requestDto.getCatalogId(), productRetailerId));
        headerDto.setParameters(carouselParameterDto);
        return headerDto;

    }

    private Component buildLimitedTimeOfferComponent(BuildTemplateRequestDto requestDto) {
        if (requestDto.getExpirationTimeMs() == null)
            throw new IllegalArgumentException("expiration_time_ms required for LTO templates");

        Component component = new Component();
        component.setType(ComponentType.LIMITED_TIME_OFFER.getValue().toLowerCase());
        component.setParameters(List.of(buildLimitedTimeOfferParam(requestDto.getExpirationTimeMs())));
        return component;
    }

    private void addCopyCodeButtonIfPresent(
            BuildTemplateRequestDto requestDto,
            Template template,
            List<Component> components) {

        if (requestDto.getCopyCode() == null)
            return;

        template.getComponents().stream()
                .filter(c -> ComponentType.BUTTONS.getValue().equalsIgnoreCase(c.getType()))
                .flatMap(c -> c.getButtons().stream())
                .filter(b -> ButtonTypes.COPY_CODE.getValue().equalsIgnoreCase(b.getType()))
                .findFirst()
                .ifPresent(button -> {
                    Component couponButton = new Component();
                    couponButton.setType("button");
                    couponButton.setSubType(button.getType().toLowerCase());
                    couponButton.setIndex(String.valueOf(button.getIndex()));
                    Parameter param = new Parameter();
                    param.setType("coupon_code");
                    param.setCouponCode(requestDto.getCopyCode());
                    couponButton.setParameters(List.of(param));
                    components.add(couponButton);
                });
    }

    private Component buildHeaderComponent(TemplateComponent comp, String mediaId, Template template,
            Map<String, String> variables) {
        if ("TEXT".equalsIgnoreCase(comp.getFormat())) {
            return buildHeaderTextComponent(template, variables);
        } else {
            return buildHeaderMediaComponent(comp.getFormat(), mediaId, template, variables);
        }
    }

    private Component buildHeaderTextComponent(Template template, Map<String, String> parameters) {
        // Get all HEADER texts
        List<TemplateText> headerTexts = template.getTexts().stream()
                .filter(t -> ComponentType.HEADER.getValue().equalsIgnoreCase(t.getType()))
                .toList();

        if (headerTexts.isEmpty())
            return null;

        List<Parameter> componentParameters = new ArrayList<>();
        for (TemplateText templateText : headerTexts) {
            String runtimeValue = parameters.get(templateText.getText());
            if (runtimeValue != null) {
                componentParameters.add(buildTextParameter(runtimeValue, Parameter::new));
            }
        }
        if (componentParameters.isEmpty())
            return null;

        Component component = new Component();
        component.setType(ComponentType.HEADER.getValue().toLowerCase());
        component.setParameters(componentParameters);
        return component;
    }

    private Component buildHeaderMediaComponent(String format, String mediaId, Template template,
            Map<String, String> parameters) {
        Component component = new Component();
        component.setType(ComponentType.HEADER.getValue().toLowerCase());
        MediaType mediaType = MediaType.fromValue(format);
        List<Parameter> componentParameters = new ArrayList<>();
        componentParameters.add(buildMediaParameter(mediaType, mediaId, Parameter::new));
        component.setParameters(componentParameters);
        return component;

    }

    private Component buildBodyComponent(Template template, Map<String, String> variables) {
        List<TemplateText> bodyTexts = template.getTexts().stream()
                .filter(t -> ComponentType.BODY.getValue().equalsIgnoreCase(t.getType()))
                .toList();

        List<Parameter> params = bodyTexts.stream()
                .map(t -> variables.get(t.getText()))
                .filter(Objects::nonNull)
                .map(val -> buildTextParameter(val, Parameter::new))
                .toList();

        if (params.isEmpty())
            return null;

        Component component = new Component();
        component.setType(ComponentType.BODY.getValue().toLowerCase());
        component.setParameters(params);
        return component;
    }

    private List<Component> buildButtonComponents(
            Template template,
            TemplateComponent templateComponent,
            Map<String, String> variables) {

        List<Component> buttonComponents = new ArrayList<>();

        for (TemplateComponentButton button : templateComponent.getButtons()) {
            switch (ButtonTypes.fromValue(button.getType())) {
                case URL -> {
                    Parameter param = template.getTexts().stream()
                            .filter(t -> "BUTTON".equalsIgnoreCase(t.getType()))
                            .map(t -> buildTextParam(variables, t.getText()))
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                    if (param != null) {
                        Component component = new Component();
                        component.setType("button");
                        component.setSubType(button.getType().toLowerCase()); // url
                        component.setIndex(String.valueOf(button.getIndex())); // button position
                        component.setParameters(List.of(param));
                        buttonComponents.add(component);
                    }
                }
            }
        }

        return buttonComponents.isEmpty() ? null : buttonComponents;
    }

    // Build-Parameters=======================================================================================

    private Language buildLanguage(String languageCode) {
        Language lang = new Language();
        lang.setCode(languageCode);
        return lang;
    }

    private <T extends TextParameter> T buildTextParameter(String text, Supplier<T> supplier) {
        T param = supplier.get();
        param.setType("text");
        param.setText(text);
        return param;
    }

    private Parameter buildCarouselTextParam(Map<String, String> parameters, String key) {
        if (parameters.containsKey(key)) {
            Parameter param = new Parameter();
            param.setType("text");
            param.setText(parameters.get(key));
            return param;
        }
        return null;
    }

    private Parameter buildCarouselQuickReplyParam(String payload) {
        Parameter param = new Parameter();
        param.setType("payload");
        param.setPayload(payload);
        return param;
    }

    private Parameter buildProductParameter(String catalogueId,
            String productRetailerId) {
        Parameter parameter = new Parameter();
        Product product = new Product();
        product.setCatalogId(catalogueId);
        product.setProductRetailerId(productRetailerId);
        parameter.setProduct(product);
        return parameter;
    }

    private <T extends MediaParameter> T buildMediaParameter(MediaType mediaType, String mediaId,
            Supplier<T> supplier) {
        T param = supplier.get();
        param.setType(mediaType.getValue().toLowerCase());

        Media mediaDto = createMediaDto(mediaType, mediaId);

        switch (mediaType) {
            case DOCUMENT -> param.setDocument((Document) mediaDto);
            case IMAGE -> param.setImage((Image) mediaDto);
            case VIDEO -> param.setVideo((Video) mediaDto);
            default -> throw new InvalidMediaType("Unsupported media type: " + mediaType);
        }
        return param;
    }

    private Media createMediaDto(MediaType mediaType, String mediaId) {
        Media mediaDto;
        switch (mediaType) {
            case DOCUMENT -> mediaDto = new Document();
            case IMAGE -> mediaDto = new Image();
            case VIDEO -> mediaDto = new Video();
            default -> throw new InvalidMediaType("Unsupported media type: " + mediaType);
        }
        mediaDto.setId(mediaId);
        return mediaDto;
    }

    private Parameter buildLimitedTimeOfferParam(Long expirationTime) {
        Parameter p = new Parameter();
        p.setType(ComponentType.LIMITED_TIME_OFFER.getValue().toLowerCase());
        p.setLimitedTimeOffer(new LimitedTimeOffer(expirationTime));
        return p;
    }

    private Parameter buildTextParam(Map<String, String> parameters, String key) {
        if (parameters.containsKey(key)) {
            Parameter param = new Parameter();
            param.setType("text");
            param.setText(parameters.get(key));
            return param;
        }
        return null;
    }

    /*
     * Used ot fetch contacts varaibles from phonebook only if it fully not
     * paramatereized from frontend means it need data from database
     */
    private PhoneBookResponseDto buildParameters(
            BuildTemplateRequestDto buildTemplateRequestDto,
            List<String> filteredMobileNumbers,
            Template template,
            Long userId,
            String defaultValue) {

        if (buildTemplateRequestDto.isFullyPrameterized()) {
            Map<String, String> sharedParams = Optional.ofNullable(buildTemplateRequestDto.getParameters())
                    .orElseGet(Map::of);

            // Build data map for DTO
            Map<String, Map<String, String>> data = filteredMobileNumbers.stream()
                    .collect(Collectors.toMap(
                            number -> number,
                            number -> new HashMap<>(sharedParams)));

            PhoneBookResponseDto dto = new PhoneBookResponseDto();
            dto.setData(data);
            return dto;
        } else {
            // In partial case we hit database and then override values from frontend
            List<String> keys = template.getTexts().stream()
                    .map(TemplateText::getText)
                    .toList();

            PhoneBookResponseDto parameters = phoneBookEntryService
                    .getParamsForPhoneNumbers(filteredMobileNumbers, keys, userId, defaultValue);

            if (buildTemplateRequestDto.getParameters() != null) {
                parameters.getData().values().forEach(map -> map.putAll(buildTemplateRequestDto.getParameters()));
            }

            return parameters;
        }
    }

    private <T> void addIfNotNull(List<T> list, T element) {
        if (element != null)
            list.add(element);
    }

    private <T> void addAllIfNotEmpty(List<T> list, List<T> elements) {
        if (elements != null && !elements.isEmpty())
            list.addAll(elements);
    }

}
