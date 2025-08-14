package com.moesif.spring.autoconfiguration;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "moesif")
public class MoesifProperties {
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
