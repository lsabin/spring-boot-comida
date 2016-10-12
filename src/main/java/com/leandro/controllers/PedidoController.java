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

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/pedido")
public class PedidoController {

	private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);
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

	// TODO: este método irá en SlackController para hacer pedidos por Slack
	@Deprecated
	@RequestMapping(method = RequestMethod.POST)
	public Pedido create(@RequestBody PedidoJsonRequest request) {

		logger.info("Request username: {}", request.getUsername());
		logger.info("Request restaurant: {}", request.getRestaurant());

		// primero buscamos si ya existe
		List<Pedido> existentes = pedidoRepository.findTodayPedidos(request.getRestaurant(), LocalDate.now().atStartOfDay(),
				LocalDate.now().atStartOfDay().plusDays(1));

		logger.info("Pedidos existentes: " + existentes);

		Pedido pedido;

		Usuario usuario = usuarioRepository.findOneByUserName(request.getUsername());


		if (existentes != null && !existentes.isEmpty()) {
			pedido = existentes.get(0);
			pedido.addUsuario(request.getUsername());
		} else {
			// Creamos uno nuevo
			pedido = new Pedido();
			pedido.setRestaurante(request.getRestaurant());
			pedido.addUsuario(request.getUsername());
			pedido.setDate(LocalDateTime.now());
		}

		if (usuario != null) {
			pedido.addPersona(usuario);
		}


		pedidoRepository.save(pedido);
		return pedido;



	}

	@RequestMapping(method = RequestMethod.POST, value = "/adduser")
	public String addUser(@RequestParam("username") String username, @RequestParam("id") String id) {

		logger.info("Request username: {}", username);
		logger.info("Request id: {}", id);

		Pedido pedido = pedidoRepository.findOne(id);
		pedido.addUsuario(username);

		Usuario usuario = usuarioRepository.findOneByUserName(username);
		if (usuario != null) {
			pedido.addPersona(usuario);
		}

		pedidoRepository.save(pedido);

		logger.info("Pedido existente: " + pedido);

		return "redirect:/";

	}

	@RequestMapping(method = RequestMethod.POST, value = "/add")
	public String addPedido(@RequestParam("username") String username, @RequestParam("restaurant") String restaurant) {

		logger.info("Request username: {}", username);
		logger.info("Request restaurant: {}", restaurant);

		Pedido pedido = new Pedido();
		pedido.setRestaurante(restaurant);
		pedido.setDate(LocalDateTime.now());
		pedido.addUsuario(username);

		Usuario usuario = usuarioRepository.findOneByUserName(username);
		if (usuario != null) {
			pedido.addPersona(usuario);
		}

		pedidoRepository.save(pedido);

		logger.info("Pedido nuevo: " + pedido);

		return "redirect:/";

	}


	@RequestMapping(method = RequestMethod.GET, value = "/lista") public String lista(HttpSession session, Model model) {
		if (session == null || session.getAttribute("username") == null) {
			model.addAttribute("slack",
					"https://slack.com/oauth/authorize?scope=users:read&client_id=3573661108.73761786598&client_secret=91b46cf8a08abf877f867ba4a0439c7a&redirect_uri=http://localhost:8888/oauthredirect");
			return "index";
		}


		model.addAttribute("pedidos", pedidoRepository.findToday(LocalDate.now().atStartOfDay(),
				LocalDate.now().atStartOfDay().plusDays(1)));
		return "index";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/all") public String listaAll(Model model) {
		model.addAttribute("pedidos", pedidoRepository.findAll());
		return "index";
	}

}
