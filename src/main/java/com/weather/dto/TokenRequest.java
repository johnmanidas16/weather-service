package com.weather.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRequest implements Serializable {
   
	private static final long serialVersionUID = -4265829862916877276L;
	
	private String username;
    private String password;
}
