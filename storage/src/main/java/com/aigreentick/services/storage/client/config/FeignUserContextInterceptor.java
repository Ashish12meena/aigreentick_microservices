package com.aigreentick.services.storage.client.config;


import com.aigreentick.services.common.context.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class FeignUserContextInterceptor implements RequestInterceptor {
     private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ORG_ID_HEADER = "X-Org-Id";

    @Override
    public void apply(RequestTemplate template) {
        Long userId = UserContext.getUserId();
        Long orgId = UserContext.getOrganisationId();

        if (userId != null) {
            template.header(USER_ID_HEADER, String.valueOf(userId));
        }

        if (orgId != null ) {
            template.header(ORG_ID_HEADER, String.valueOf(orgId));
        }
    }
}
