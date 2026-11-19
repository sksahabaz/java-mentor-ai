package com.example.javamentor.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.javamentor.entity.Role;
import com.example.javamentor.repository.RoleRepository;

@Component
@Order(1)
public class RoleSeeder implements CommandLineRunner {
	private final RoleRepository roleRepository;

	public RoleSeeder(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	@Override
	public void run(String... args) {
		for (String name : List.of(Role.STUDENT, Role.ADMIN)) {
			if (roleRepository.findByName(name).isEmpty()) {
				roleRepository.save(new Role(name));
			}
		}
	}
}
