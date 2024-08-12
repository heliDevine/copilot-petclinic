package org.springframework.samples.petclinic.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/**
	 * Retrieves a list of all owners.
	 *
	 * @return a list of Owner objects.
	 */
	@RequestMapping(value = "/owners", produces = "application/json")
	public List<Owner> getOwners() {
		try {
			List<Owner> ownerList = jdbcTemplate.query("select id, first_name, last_name from owners", (rs, rowNum) -> {
				Owner o = new Owner();
				o.setId(rs.getInt("id"));
				o.setFirstName(rs.getString("first_name"));
				o.setLastName(rs.getString("last_name"));
				return o;
			}).stream().toList();
			return ownerList;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving owners", e);
		}
	}

	/**
	 * Retrieves a list of pets by their name.
	 *
	 * @param name the name of the pet.
	 * @return a list of maps containing pet details.
	 */
	@RequestMapping(value = "/pets/{name}", produces = "application/json")
	public List<Map<String, Object>> getPetsByName(@PathVariable("name") String name) {
		if (name == null || name.trim().isEmpty() || name.matches(".*[;'\"]+.*")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pet name");
		}
		try {
			List<Map<String, Object>> pets = jdbcTemplate
				.queryForList("select id, name, birth_date from pets where name = ?", name);
			return pets;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving pets by name", e);
		}
	}

	/**
	 * Retrieves a list of pets for a specific owner.
	 *
	 * @param ownerId the ID of the owner.
	 * @return a list of Pet objects.
	 */
	@RequestMapping(value = "/owners/{ownerId}/pets", produces = "application/json")
	public List<Pet> getPetsByOwner(@PathVariable("ownerId") Integer ownerId) {
		if (ownerId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner ID cannot be null");
		}
		try {
			List<Pet> pets = jdbcTemplate.query("select id, name, birth_date from pets where owner_id = ?",
				new Object[]{ownerId}, (rs, rowNum) -> {
					Pet p = new Pet();
					p.setId(rs.getInt("id"));
					p.setName(rs.getString("name"));
					p.setBirthDate(rs.getDate("birth_date").toLocalDate());
					return p;
				});
			return pets;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving pets by owner", e);
		}
	}

	@RequestMapping(value = "/drugs", produces = "application/json")
	public List<Map<String, Object>> getDrugs() {
		try {
			List<Map<String, Object>> drugs = jdbcTemplate
				.queryForList("SELECT id, name, description, price FROM drugs ORDER BY price");
			return drugs;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving drugs", e);
		}
	}
}
