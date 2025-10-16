package com.aigreentick.services.common.context;



/**
 * Holds user and organisation information for the current request.
 * This object is stored in ThreadLocal via UserContext.
 */
public class UserContextData {

    private Long userId;
    private Long organisationId; // optional for multi-tenant setups

    public UserContextData() {}

    public UserContextData(Long userId, Long organisationId) {
        this.userId = userId;
        this.organisationId = organisationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }
}
