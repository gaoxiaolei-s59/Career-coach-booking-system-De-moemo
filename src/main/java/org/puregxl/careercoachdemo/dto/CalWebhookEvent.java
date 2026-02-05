package org.puregxl.careercoachdemo.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CalWebhookEvent {
    private String triggerEvent;
    private Payload payload;
    private String createdAt;

    @Data
    public static class Payload {
        private Long id;
        private String uid;
        private String title;
        private String startTime;
        private String endTime;
        private List<Attendee> attendees;
        private Organizer organizer;
        private Map<String, Object> metadata;
        private String cancellationReason;
        private String cancelUrl;
    }

    @Data
    public static class Attendee {
        private String email;
        private String name;
        private String timeZone;
    }

    @Data
    public static class Organizer {
        private String email;
        private String name;
        private String timeZone;
    }
}
