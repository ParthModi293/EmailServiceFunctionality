package com.example.emailservice.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data

public class EmailDto {
    private List<String> to;
    private String from;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private Map<String, String> placeholders;
    private List<MultipartFile> attachments;
    private String version;


}
