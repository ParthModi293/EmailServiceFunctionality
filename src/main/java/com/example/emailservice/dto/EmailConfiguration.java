package com.example.emailservice.dto;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="email_configuration")
public class EmailConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "host")
    private String host;

    @Column(name = "port")
    private int port;

    @Column(name = "user_name")
    private String username;

    @Column(name = "password")
    private String password;

}
