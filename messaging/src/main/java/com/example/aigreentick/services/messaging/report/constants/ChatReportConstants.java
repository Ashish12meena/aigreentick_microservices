package com.example.aigreentick.services.messaging.report.constants;

public final class ChatReportConstants {

    private ChatReportConstants() {} // Prevent instantiation

    // --- Mongo DB Field Names ---
    public static final class Fields {
        private Fields() {}

        public static final String USER_ID = "userId";
        public static final String CAMPAIGN_ID = "campaignId";
        public static final String BROADCAST_ID = "broadcastId";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String CREATED_AT = "createdAt";
        public static final String DELETED_AT = "deletedAt";
        public static final String MESSAGE_ID = "messageId";
        public static final String ID = "id";
    }

    //  Error Messages ---
    public static final class ErrorMessages {
        private ErrorMessages() {}

        public static final String REPORT_NOT_FOUND = "Report not found with id: %s";
        public static final String USER_NOT_FOUND = "User not found with id: %d";
    }

    //  Other Constants ---
    public static final class Status {
        private Status() {}

        public static final String PENDING = "pending";
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
    }

     // --- Base Paths ---
    public static final class Paths {
        private Paths() {}
        public static final String BASE = "api/report";
        public static final String USER_FILTERED = "/user/filtered";
        public static final String MY_SUMMARY = "/my/summary";
    }

     // --- Default Values ---
    public static final class Defaults {
        private Defaults() {}
        public static final String PAGE = "0";
        public static final String SIZE = "10";
    }
}
