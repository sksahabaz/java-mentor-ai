package com.example.javamentor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.javamentor.entity.Role;
import com.example.javamentor.entity.Student;
import com.example.javamentor.repository.RoleRepository;
import com.example.javamentor.repository.StudentRepository;

@Component
@Order(2)
public class AdminSeeder implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

	private final StudentRepository studentRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final String email;
	private final String password;

	public AdminSeeder(StudentRepository studentRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder,
			@Value("${app.admin.email:}") String email,
			@Value("${app.admin.password:}") String password) {
		this.studentRepository = studentRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.email = email.trim().toLowerCase();
		this.password = password;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (email.isBlank() || password.isBlank()) {
			log.info("ADMIN_EMAIL/ADMIN_PASSWORD not set - skipping admin seeding");
			return;
		}
		if (studentRepository.existsByEmail(email)) {
			return;
		}
		Role admin = roleRepository.findByName(Role.ADMIN)
				.orElseThrow(() -> new IllegalStateException("ROLE_ADMIN missing"));
		Role student = roleRepository.findByName(Role.STUDENT)
				.orElseThrow(() -> new IllegalStateException("ROLE_STUDENT missing"));

		Student user = new Student();
		user.setName("Admin");
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setEnabled(true);
		user.addRole(admin);
		user.addRole(student);
		studentRepository.save(user);
		log.info("Admin user created: {}", email);
	}
}
