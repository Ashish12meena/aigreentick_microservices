package com.aigreentick.services.template.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.aigreentick.services.common.context.UserContext;
import com.aigreentick.services.common.util.JsonUtils;
import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.BaseTemplateRequestDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.AuthBodyComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.AuthButtonComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.AuthButtonDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.AuthFooterComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.authentication.SupportedAppDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingBodyComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingButtonDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingButtonsComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingFooterComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.MarketingHeaderComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselButtonDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselButtonsComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselCardComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselCardDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.marketing.carousel.CarouselHeaderComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityBodyComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityButtonDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityButtonsComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityFooterComponentDto;
import com.aigreentick.services.template.dto.buildTemplateForRequest.utility.UtilityHeaderComponentDto;
import com.aigreentick.services.template.dto.request.TemplateTextDto;
import com.aigreentick.services.template.dto.response.TemplateResponseDto;
import com.aigreentick.services.template.enums.ButtonTypes;
import com.aigreentick.services.template.enums.ComponentType;
import com.aigreentick.services.template.enums.MediaFormat;
import com.aigreentick.services.template.enums.OtpTypes;
import com.aigreentick.services.template.enums.TemplateCategory;
import com.aigreentick.services.template.enums.TemplateStatus;
import com.aigreentick.services.template.model.SupportedApp;
import com.aigreentick.services.template.model.Template;
import com.aigreentick.services.template.model.TemplateComponent;
import com.aigreentick.services.template.model.TemplateComponentButton;
import com.aigreentick.services.template.model.TemplateExample;
import com.aigreentick.services.template.model.TemplateText;
import com.aigreentick.services.template.model.carousel.TemplateCarouselButton;
import com.aigreentick.services.template.model.carousel.TemplateCarouselCardComponent;
import com.aigreentick.services.template.model.carousel.TemplateCarouselExample;
import com.aigreentick.services.template.model.carousel.TemplateComponentCards;

@Component
public class TemplateMapper {

    public TemplateMapper() {
    }

    public TemplateResponseDto toTemplateDto(Template template) {
        TemplateResponseDto templateDto = new TemplateResponseDto();
        templateDto.setId(template.getId());
        templateDto.setName(template.getName());
        templateDto.setStatus(template.getStatus().name()); // Enum to String
        templateDto.setCategory(template.getCategory());
        templateDto.setLanguage(template.getLanguage());
        templateDto.setMetaTemplateId(template.getMetaTemplateId());
        return templateDto;
    }

    public List<TemplateResponseDto> toTemplateDtoList(List<Template> templates) {
        return templates.stream().map(this::toTemplateDto).toList();
    }

    public Template toTemplateEntity(TemplateResponseDto dto, Long user) {
        Template template = new Template();
        template.setName(dto.getName());
        template.setCategory(dto.getCategory());
        template.setLanguage(dto.getLanguage());
        template.setStatus(TemplateStatus.PENDING);
        return template;
    }

    public Template toTemplateEntity(BaseTemplateRequestDto dto,Long userId) {
        if (dto == null) {
            return null;
        }
        Template template = toTemplateEntity(userId,null, dto, null, dto.getCategory().getValue(),null);
        template.setStatus(TemplateStatus.valueOf(dto.getStatus().toUpperCase()));
        template.setCategory(dto.getCategory().getValue());
        template.setMetaTemplateId(dto.getId());
        return template;

    }

    public List<Template> toTemplateEntityList(List<BaseTemplateRequestDto> dtos,Long userId) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        return dtos.stream()
                .map(dto->toTemplateEntity(dto, userId))
                .collect(Collectors.toList());
    }

    public Template toTemplateEntity(
            Long userId,
            String jsonRequest,
            BaseTemplateRequestDto dto,
            String data,
            String category, 
            List<TemplateTextDto> templateTextsDto) {

        String componentsJson = JsonUtils.serializeToString(dto);
        List<? extends BaseComponentDto> components = dto.getComponents();
        Template template = new Template();
        template.setName(dto.getName());
        template.setUserId(userId);
        template.setOrganisationId(UserContext.getOrganisationId());
        template.setLanguage(dto.getLanguage());
        template.setSubmissionPayload(jsonRequest);
        template.setComponentsJson(componentsJson);
        template.setResponse(data);
        List<TemplateComponent> templateComponents = new ArrayList<>();

        if (components == null || components.isEmpty()) {
            return template;
        }
        TemplateCategory templateCategory = TemplateCategory.fromValue(category);

        switch (templateCategory) {
            case AUTHENTICATION -> {
                setAuthenticationTemplate(components, templateComponents);
                template.setCategory(templateCategory.getValue()); // lowercase for DB/API
            }
            case MARKETING -> {
                setMarketingTemplate(components, templateComponents);
                template.setCategory(templateCategory.getValue());
            }
            case UTILITY -> {
                setUtilityTemplate(components, templateComponents);
                template.setCategory(templateCategory.getValue());
            }
            default -> throw new IllegalArgumentException("Unsupported category: " + category);
        }

        if (templateTextsDto!=null) {
            List<TemplateText> templateTexts = new ArrayList<>();
            for (TemplateTextDto templateTextDto : templateTextsDto) {
                TemplateText templateText = new TemplateText();
                templateText.setText(templateTextDto.getText());
                templateText.setType(templateTextDto.getType());
                templateText.setTextIndex(templateTextDto.getVariableIndex());
                templateTexts.add(templateText);
            }
            template.setTexts(templateTexts);
        }

        template.setComponents(templateComponents);

        return template;
    }

    private void setUtilityTemplate(List<? extends BaseComponentDto> components,
            List<TemplateComponent> templateComponents) {
        for (BaseComponentDto component : components) {
            TemplateComponent templateComponent = new TemplateComponent();
            if (component instanceof UtilityHeaderComponentDto utilityHeader) {
                if (utilityHeader.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(utilityHeader.getType()));
                }
                if (utilityHeader.getText() != null) {
                    templateComponent.setText(utilityHeader.getText());
                }
                if (utilityHeader.getFormat() != null) {
                    templateComponent.setFormat(utilityHeader.getFormat());
                }
                if (utilityHeader.getExample() != null) {
                    TemplateExample templateExample = new TemplateExample();
                    if (utilityHeader.getExample().getHeaderText() != null) {
                        templateExample.setHeaderHandle(utilityHeader.getExample().getHeaderText());
                    }
                    if (utilityHeader.getExample().getHeaderHandle() != null) {
                        templateExample.setHeaderText(utilityHeader.getExample().getHeaderText());
                    }
                    templateComponent.setExample(templateExample);
                }
                templateComponents.add(templateComponent);
            } else if (component instanceof UtilityBodyComponentDto utilityBody) {
                if (utilityBody.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(utilityBody.getType()));
                }
                if (utilityBody.getText() != null) {
                    templateComponent.setText(utilityBody.getText());
                }
                if (utilityBody.getExample() != null) {
                    TemplateExample templateExample = new TemplateExample();
                    if (templateExample.getBodyText() != null) {
                        templateExample.setBodyText(templateExample.getBodyText());
                    }
                    templateComponent.setExample(templateExample);
                }
                templateComponents.add(templateComponent);
            } else if (component instanceof UtilityFooterComponentDto utilityFooter) {
                if (utilityFooter.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(utilityFooter.getType()));
                }
                if (utilityFooter.getText() != null) {
                    templateComponent.setText(utilityFooter.getText());
                }
                templateComponents.add(templateComponent);
            } else if (component instanceof UtilityButtonsComponentDto utilityButtons) {
                if (utilityButtons.getButtons() != null && !utilityButtons.getButtons().isEmpty()) {
                    List<TemplateComponentButton> templateComponentButtons = new ArrayList<>();
                    for (UtilityButtonDto buttonDto : utilityButtons.getButtons()) {
                        TemplateComponentButton button = new TemplateComponentButton();
                        if (buttonDto.getType() != null) {
                            button.setType(button.getType());
                        }
                        if (buttonDto.getType() != null) {
                            button.setType(button.getType());
                        }
                        if (buttonDto.getText() != null) {
                            button.setText(button.getText());
                        }
                        if (buttonDto.getUrl() != null) {
                            button.setUrl(button.getUrl());
                        }
                        if (buttonDto.getPhoneNumber() != null) {
                            button.setPhoneNumber(button.getPhoneNumber());
                        }
                        if (buttonDto.getExample() != null) {
                            button.setExample(button.getExample());
                        }
                        templateComponentButtons.add(button);
                    }
                    templateComponent.setButtons(templateComponentButtons);
                }
                templateComponents.add(templateComponent);
            }
        }
    }

    private void setMarketingTemplate(List<? extends BaseComponentDto> components,
            List<TemplateComponent> templateComponents) {

        for (BaseComponentDto component : components) {
            TemplateComponent templateComponent = new TemplateComponent();

            // ---------------- Marketing ----------------
            // Marketing header
            if (component instanceof MarketingHeaderComponentDto marketingHeader) {
                if (marketingHeader.getText() != null) {
                    templateComponent.setText(marketingHeader.getText());
                }
                if (marketingHeader.getFormat() != null) {
                    templateComponent.setFormat(marketingHeader.getFormat());
                }
                if (marketingHeader.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(marketingHeader.getType()));
                }
                if (marketingHeader.getExample() != null) {
                    TemplateExample templateExample = new TemplateExample();
                    if (marketingHeader.getExample().getHeaderHandle() != null) {
                        templateExample.setHeaderHandle(marketingHeader.getExample().getHeaderHandle());
                    }
                    if (marketingHeader.getExample().getHeaderText() != null) {
                        templateExample.setHeaderText(marketingHeader.getExample().getHeaderHandle());
                    }
                    templateComponent.setExample(templateExample);
                }
            } else if (component instanceof MarketingBodyComponentDto marketingBody) {
                if (marketingBody.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(marketingBody.getType()));
                }
                if (marketingBody.getText() != null) {
                    templateComponent.setText(marketingBody.getText());
                }
                if (marketingBody.getExample() != null) {
                    TemplateExample templateExample = new TemplateExample();
                    if (marketingBody.getExample().getBodyText() != null) {
                        templateExample.setBodyText(marketingBody.getExample().getBodyText());
                    }
                    templateComponent.setExample(templateExample);
                }
            } else if (component instanceof MarketingButtonsComponentDto marketingButtons) {
                marketingButtons.setType("BUTTONS");
                if (marketingButtons.getButtons() != null && !marketingButtons.getButtons().isEmpty()) {
                    int index =0;
                    List<TemplateComponentButton> templateButtons = new ArrayList<>();
                    for (MarketingButtonDto button : marketingButtons.getButtons()) {
                        TemplateComponentButton templateButton = new TemplateComponentButton();
                        if (button.getType() != null) {
                            templateButton.setType(ButtonTypes.fromValue(button.getType()));
                        }
                        if (button.getText() != null) {
                            templateButton.setText(button.getText());
                        }
                        if (button.getUrl() != null) {
                            templateButton.setUrl(button.getUrl());
                        }
                        if (button.getPhoneNumber() != null) {
                            templateButton.setPhoneNumber(button.getPhoneNumber());
                        }
                        if (button.getExample() != null) {
                            templateButton.setExample(button.getExample());
                        }
                        templateButton.setIndex(index);
                        templateButtons.add(templateButton);
                        index++;
                    }
                    templateComponent.setButtons(templateButtons);
                }
            }
            if (component instanceof MarketingFooterComponentDto marketingFooter) {
                if (marketingFooter.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(marketingFooter.getType()));
                }
                if (marketingFooter.getText() != null) {
                    templateComponent.setText(marketingFooter.getText());
                }
            } else if (component instanceof CarouselComponentDto carousel) {
                if (carousel.getCards() != null && !carousel.getCards().isEmpty()) {
                    List<TemplateComponentCards> cards = new ArrayList<>();

                    for (CarouselCardDto cardDto : carousel.getCards()) {
                        TemplateComponentCards card = new TemplateComponentCards();
                        List<TemplateCarouselCardComponent> carouselComponents = new ArrayList<>();
                        for (CarouselCardComponentDto carouselComponentDto : cardDto.getComponents()) {
                            TemplateCarouselCardComponent carouselComponent = new TemplateCarouselCardComponent();
                            if (carouselComponentDto instanceof CarouselHeaderComponentDto carouselHeader) {
                                if (carouselHeader.getType() != null) {
                                    carouselComponent.setType(carouselHeader.getType());
                                }
                                if (carouselHeader.getFormat() != null) {
                                    carouselComponent.setFormat(MediaFormat.fromValue(carouselHeader.getFormat()));
                                }
                                if (carouselHeader.getExample() != null) {
                                    TemplateCarouselExample carouselExample = new TemplateCarouselExample();
                                    carouselExample.setHeaderHandle(carouselHeader.getExample().getHeaderHandle());
                                    carouselComponent.setExample(carouselExample);
                                }
                                carouselComponents.add(carouselComponent);

                            } else if (carouselComponentDto instanceof CarouselButtonsComponentDto carouselButtonsDto) {
                                List<TemplateCarouselButton> carouselButtons = new ArrayList<>();
                                for (CarouselButtonDto button : carouselButtonsDto.getButtons()) {
                                    TemplateCarouselButton carouselButton = new TemplateCarouselButton();
                                    if (button.getType() != null) {
                                        carouselButton.setType(ButtonTypes.fromValue(button.getType()));
                                    }
                                    if (button.getText() != null) {
                                        carouselButton.setText(button.getText());
                                    }
                                    if (button.getUrl() != null) {
                                        carouselButton.setUrl(button.getUrl());
                                    }
                                    if (button.getPhoneNumber() != null) {
                                        carouselButton.setPhoneNumber(button.getPhoneNumber());
                                    }
                                    if (button.getExample() != null) {
                                        carouselButton.setExample(button.getExample());
                                    }
                                    carouselButtons.add(carouselButton);
                                }
                                carouselComponent.setButtons(carouselButtons);
                                carouselComponent.setType(carouselButtonsDto.getType());
                                carouselComponents.add(carouselComponent);

                            }
                        }
                        card.setComponents(carouselComponents);
                        cards.add(card);
                    }
                    templateComponent.setType(ComponentType.fromValue(carousel.getType()));
                    templateComponent.setCards(cards);

                }
            }
            templateComponents.add(templateComponent);

        }
    }

    private void setAuthenticationTemplate(List<? extends BaseComponentDto> components,
            List<TemplateComponent> templateComponents) {
        for (BaseComponentDto component : components) {
            TemplateComponent templateComponent = new TemplateComponent();

            // ---------------- AUTHENTICATION ----------------
            // Authentication Body
            if (component instanceof AuthBodyComponentDto authBody) {
                if (authBody.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(authBody.getType()));
                }
                if (authBody.getAddSecurityRecommendation() != null) {
                    templateComponent.setAddSecurityRecommendation(authBody.getAddSecurityRecommendation());
                }
            } else
            // Authentication Footer
            if (component instanceof AuthFooterComponentDto authFooter) {
                if (authFooter.getType() != null) {
                    templateComponent.setType(ComponentType.fromValue(authFooter.getType()));
                }
                if (authFooter.getCodeExpirationMinutes() != null) {
                    templateComponent.setCodeExpirationMinutes(authFooter.getCodeExpirationMinutes());
                }
            } else
            // Authentication Buttons
            if (component instanceof AuthButtonComponentDto authButton) {
                if (authButton.getButtons() != null && !authButton.getButtons().isEmpty()) {
                    List<TemplateComponentButton> templateButtons = new ArrayList<>();
                    for (AuthButtonDto button : authButton.getButtons()) {
                        TemplateComponentButton templateButton = new TemplateComponentButton();

                        if (button.getText() != null) {
                            templateButton.setText(button.getText());
                        }
                        if (button.getType() != null) {
                            templateButton.setType(ButtonTypes.fromValue(button.getType()));
                        }
                        if (button.getOtpType() != null) {
                            templateButton.setOtpType(OtpTypes.fromValue(button.getOtpType()));
                        }
                        if (button.getAutofillText() != null) {
                            templateButton.setAutofillText(button.getAutofillText());
                        }

                        if (button.getSupportedApps() != null) {
                            List<SupportedApp> supportedApps = new ArrayList<>();
                            for (SupportedAppDto sAppDto : button.getSupportedApps()) {
                                SupportedApp supportedApp = new SupportedApp();
                                supportedApp.setPackageName(sAppDto.getPackageName());
                                supportedApp.setSignatureHash(sAppDto.getSignatureHash());
                                supportedApps.add(supportedApp);
                            }
                            templateButton.setSupportedApps(supportedApps);
                        }

                        templateButtons.add(templateButton);
                    }
                    if (authButton.getType() != null) {
                        templateComponent.setType(ComponentType.fromValue(authButton.getType()));
                    }
                    templateComponent.setButtons(templateButtons);
                }
            }

            templateComponents.add(templateComponent);
        }
    }

}
