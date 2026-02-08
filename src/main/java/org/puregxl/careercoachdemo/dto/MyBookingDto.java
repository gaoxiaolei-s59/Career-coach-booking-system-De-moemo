package org.puregxl.careercoachdemo.dto;

import lombok.Data;

import java.time.LocalDateTime;

//1111111
@Data
public class MyBookingDto {
    private String status;
    private String coachName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
