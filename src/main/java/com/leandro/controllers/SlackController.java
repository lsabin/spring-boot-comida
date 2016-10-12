package com.leandro.controllers;

import com.leandro.controllers.db.PedidoRepository;
import com.leandro.controllers.db.UsuarioRepository;
import com.leandro.model.Pedido;
import com.leandro.model.PedidoJsonRequest;
import com.leandro.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/listado")
public class SlackController {

	private static final Logger logger = LoggerFactory.getLogger(SlackController.class);
	private PedidoRepository pedidoRepository;
	private UsuarioRepository usuarioRepository;

	@Autowired(required = true)
	public void setPedidoRepository(PedidoRepository pedidoRepository) {
		this.pedidoRepository = pedidoRepository;
	}

	@Autowired(required = true)
	public void setUsuarioRepository(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}


	@RequestMapping(method = RequestMethod.GET)
	public String listado() {
		List<Pedido> pedidos = pedidoRepository.findToday(LocalDate.now().atStartOfDay(),
				LocalDate.now().atStartOfDay().plusDays(1));

		logger.info("Pedidos existentes: " + pedidos);

		if (pedidos != null && pedidos.size() > 0) {
			List<String> cadenas = new ArrayList<>();

			pedidos.forEach(pedido -> {
				String resultado = pedido.getRestaurante().concat(": ");
				String gente = pedido.getPersonas().stream().map(Usuario::getNombre).collect(Collectors.joining(", "));
				resultado = resultado.concat(gente);
				cadenas.add(resultado);

			});

			return cadenas.stream().collect(Collectors.joining("\n"));


		} else {
			return "Nadie quiere pedir comida todavía.";
		}

	}

}
