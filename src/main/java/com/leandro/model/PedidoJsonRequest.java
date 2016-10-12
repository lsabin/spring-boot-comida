package com.leandro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PedidoJsonRequest {
	private String restaurant;

	@JsonProperty("user_name")
	private String username;

	private String id;

	public String getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(String restaurant) {
		this.restaurant = restaurant;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
