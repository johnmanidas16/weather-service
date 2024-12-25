package com.weather.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest implements Serializable {
    
	private static final long serialVersionUID = -5769774221974042208L;
	
	private String username;
    private String password;
    private String postalCode;
}
