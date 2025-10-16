package com.aigreentick.services.common.model.feature;

public interface TenantAware {
    Long getOrganisationId();
    void setOrganisationId(Long organisationId);
}
