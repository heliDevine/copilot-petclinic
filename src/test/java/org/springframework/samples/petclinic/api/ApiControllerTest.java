package org.springframework.samples.petclinic.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiControllerTest {

	@LocalServerPort
	int port;

	@Autowired
	private RestTemplateBuilder builder;

	@Test
	void testGetOwners() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> result = template.exchange(RequestEntity.get("/api/owners").build(), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(Objects.requireNonNull(result.getHeaders().getContentType()).toString()).isEqualTo("application/json");
	}

	@Test
	void testGetPetsByNameSqlInjection() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		String maliciousName = "name'; DROP TABLE pets; --";
		try {
			template.exchange(RequestEntity.get("/api/pets/" + maliciousName).build(), String.class);
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		}
	}
}
