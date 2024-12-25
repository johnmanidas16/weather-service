package com.weather.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "users")
@Data
public class User {
	@Id
	private String id;
	private String username;
	private String password;
	private String postalCode;
	private boolean active;
	private List<String> roles;
}
