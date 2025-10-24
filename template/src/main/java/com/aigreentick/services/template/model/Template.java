package com.aigreentick.services.template.model;

import java.util.List;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.aigreentick.services.common.model.base.MongoBaseEntity;
import com.aigreentick.services.common.model.feature.TenantAware;
import com.aigreentick.services.template.enums.TemplateStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


/**
 * MongoDB entity representing a WhatsApp template.
 * <p>
 * Includes audit, tenant-awareness, and indexing for efficient querying.
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "templates")
public class Template extends MongoBaseEntity implements TenantAware{


    @Indexed(name = "idx_templates_name")
    private String name;

    private String category;

    private String language;

    private TemplateStatus status;  // Pending , Approved , Rejected

    private String rejectionReason;

    private String previousCategory;

    private String metaTemplateId;

    private String submissionPayload;

    private List<TemplateComponent> components;

    private List<TemplateText> texts;

    private String response;

    private String componentsJson;

    @Indexed(name = "idx_templates_user_id")
    private Long userId;

    @Indexed(name = "idx_templates_organisation")
    private Long organisationId;

}
