package com.aigreentick.services.common.context;


/**
 * Thread-local holder for UserContextData.
 * Keeps user and organisation info isolated per request thread.
 */
public class UserContext {

    private static final ThreadLocal<UserContextData> CONTEXT = new ThreadLocal<>();

    private UserContext() {
        // prevent instantiation
    }

    public static void set(UserContextData data) {
        CONTEXT.set(data);
    }

    public static UserContextData get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        UserContextData data = CONTEXT.get();
        return data != null ? data.getUserId() : null;
    }

    public static Long getOrganisationId() {
        UserContextData data = CONTEXT.get();
        return data != null ? data.getOrganisationId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
