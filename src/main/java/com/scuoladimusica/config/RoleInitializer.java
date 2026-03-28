package com.scuoladimusica.config;

import com.scuoladimusica.model.entity.ERole;
import com.scuoladimusica.model.entity.Role;
import com.scuoladimusica.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_STUDENT));
            roleRepository.save(new Role(ERole.ROLE_TEACHER));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
    }
}