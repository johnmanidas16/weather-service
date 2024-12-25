package com.weather.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TokenRequest implements Serializable {
   
	private static final long serialVersionUID = -4265829862916877276L;
	
	private String username;
    private String password;
}
