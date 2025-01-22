package com.example.emailservice.repository;

import com.example.emailservice.dto.EmailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, Integer> {

    EmailConfiguration findByUsername(String username);

}
