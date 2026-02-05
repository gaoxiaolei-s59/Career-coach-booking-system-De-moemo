package org.puregxl.careercoachdemo.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalWebhookRequest {
    private String triggerEvent; // BOOKING_CREATED, BOOKING_CANCELLED
    private LocalDateTime time;
    private Payload payload;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        private String uid; // Cal Booking UID
        private String startTime;
        private String endTime;
        private Map<String, String> metadata; // 这里存放我们传过去的 userId
        private Attendee[] attendees;
        private Organizer organizer; // Coach info
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attendee {
        private String name;
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Organizer {
        private String name;
        private String email;
    }
}