
package com.wedding.gallery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeddingGalleryApplication {

	@org.springframework.beans.factory.annotation.Value("${app.admin.username}")
	private String adminUsername;

	@org.springframework.beans.factory.annotation.Value("${app.admin.password}")
	private String adminPassword;

	public static void main(String[] args) {
		SpringApplication.run(WeddingGalleryApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.CommandLineRunner initData(
			com.wedding.gallery.repository.UserRepository userRepository,
			org.springframework.security.crypto.password.PasswordEncoder encoder) {
		return args -> {
			com.wedding.gallery.model.User admin = userRepository.findByUsername(adminUsername).orElse(null);
			if (admin == null) {
				admin = new com.wedding.gallery.model.User();
				admin.setUsername(adminUsername);
				admin.setRole("ROLE_ADMIN");
			}
			admin.setPassword(encoder.encode(adminPassword));
			userRepository.save(admin);
			System.out.println("Admin credentials updated/created for: " + adminUsername);
		};
	}
}
// mvn spring-boot:run