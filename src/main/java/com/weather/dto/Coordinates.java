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
public class Coordinates implements Serializable {
    
	private static final long serialVersionUID = 4810436007791022864L;
	
	private String zip;
    private String name;
    private long lat;
    private long lon;
    private String country;
}
