package com.example.emailservice.bean;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Data
public class EmailBean {

    private String templateName;
    private String dbName;
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private Map<String, Object> subjectPlaceHolder;
    private Map<String, Object> bodyPlaceHolder;
    private List<MultipartFile> file;

}
