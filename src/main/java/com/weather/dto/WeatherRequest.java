package com.weather.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherRequest implements Serializable {
	
	private static final long serialVersionUID = -4033330753724257109L;
	
	@Pattern(regexp = "^\\d{5}$", message = "Invalid US postal code format")
	@NotBlank
	private String postalCode;
	@NotBlank
	private String username;
}
