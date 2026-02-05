package org.puregxl.careercoachdemo.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Booking {
    private Long id;
    private String userId;
    private String coachName;
    private String coachEmail;
    private String userEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String calBookingUid;
    private BookingStatus status;
}
