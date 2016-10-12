package com.leandro.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document
public class Pedido {

	@Id
	private String id;
	private LocalDateTime date;
	private String restaurante;
	private List<String> usuarios = new ArrayList<>();
	private List<Usuario> personas = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public String getRestaurante() {
		return restaurante;
	}

	public void setRestaurante(String restaurante) {
		this.restaurante = restaurante;
	}

	public List<String> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(List<String> usuarios) {
		this.usuarios = usuarios;
	}

	public void addUsuario(String usuario) {
		usuarios.add(usuario);
	}

	public List<Usuario> getPersonas() {
		return personas;
	}

	public void setPersonas(List<Usuario> personas) {
		this.personas = personas;
	}

	public void addPersona(Usuario usuario) {
		personas.add(usuario);
	}
}

