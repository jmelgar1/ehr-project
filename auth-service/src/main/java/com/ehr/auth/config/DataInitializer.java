package com.ehr.auth.config;

import com.ehr.auth.model.Permission;
import com.ehr.auth.repository.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    public DataInitializer(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        createPermissionIfNotExists("USER", "DELETE", "Allows deleting user accounts");
    }

    private void createPermissionIfNotExists(String resource, String action, String description) {
        if (permissionRepository.findByResourceAndAction(resource, action).isEmpty()) {
            Permission permission = Permission.builder()
                    .resource(resource)
                    .action(action)
                    .description(description)
                    .build();
            permissionRepository.save(permission);
        }
    }
}
