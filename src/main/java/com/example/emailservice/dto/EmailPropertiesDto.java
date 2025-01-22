package com.example.emailservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailPropertiesDto {

    private String username;
    private String password;
    private String host;
    private int port;
}
