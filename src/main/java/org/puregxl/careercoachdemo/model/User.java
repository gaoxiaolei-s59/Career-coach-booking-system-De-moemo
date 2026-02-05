package org.puregxl.careercoachdemo.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private String id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
