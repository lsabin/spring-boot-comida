package com.leandro.controllers;

import com.leandro.controllers.db.PedidoRepository;
import com.leandro.controllers.db.UsuarioRepository;
import com.leandro.model.Usuario;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class IndexController {
	private static final Logger logger = LoggerFactory.getLogger(IndexController.class);


	@Value("${slack.client_secret}")
	private String client_secret;

	@Value("${slack.redirect_url}")
	private String redirect_token;

	@Value("${slack.client_id}")
	private String client_id;



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

	@RequestMapping("/")
	public String index(HttpSession session, Model model) {
		if (session == null || session.getAttribute("username") == null) {

			String authenticationUrl = "https://slack.com/oauth/authorize?scope=users:read&"
					.concat("client_id=").concat(client_id).concat("&")
					.concat("client_secret=").concat(client_secret).concat("&")
					.concat("redirect_uri=").concat(redirect_token);

			model.addAttribute("slack", authenticationUrl);
			return "index";
		} else {
			return "forward:/pedido/lista";
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/oauthredirect")
	public String redirectedFromOauth(HttpServletRequest request, HttpSession session) {

		String code = request.getParameter("code");
		logger.info("Returned code: {}", code);


		if (code != null) {
			UserInfo userInfo = sendAuth(code);

			if (userInfo != null) {
				JSONObject resJson = getUserInfo(userInfo.getToken(), userInfo.getUserId());

				if (resJson != null) {
					String name = resJson.getJSONObject("user").getString("name");
					String img = resJson.getJSONObject("user").getJSONObject("profile").getString("image_32");
					String realName = resJson.getJSONObject("user").getJSONObject("profile").getString("real_name");

					session.setAttribute("username", name);
					session.setAttribute("avatar", img);
					session.setAttribute("nombre", realName);

					Usuario usuario = usuarioRepository.findOneByUserName(name);

					if (usuario != null) {
						usuario.setAvatar(img);
					} else {
						usuario = new Usuario();
						usuario.setAvatar(img);
						usuario.setNombre(realName);
						usuario.setUserName(name);
					}

					usuarioRepository.save(usuario);
				}
			}
		}

		return "redirect:/pedido/lista";

	}

	private UserInfo sendAuth(String code) {
		String url = "https://slack.com/api/oauth.access";
		HttpRequest request = Unirest.get(url);

		logger.info("Client_id: {}", client_id);
		request.queryString("client_id", client_id);
		request.queryString("client_secret", client_secret);
		request.queryString("code", code);
		request.queryString("redirect_uri", redirect_token);

		Optional<JSONObject> respuestaJson = sendRequest(request);

		if (respuestaJson.isPresent()) {
			logger.info("Respuesta: {}", respuestaJson.get());
			logger.info("access_token: {}", respuestaJson.get().getString("access_token"));

			String token = respuestaJson.get().getString("access_token");
			String userId = respuestaJson.get().getString("user_id");

			return new UserInfo(token, userId);
		} else {
			return null;
		}
	}

	private JSONObject getUserInfo(String token, String userId) {
		String url = "https://slack.com/api/users.info";
		HttpRequest request = Unirest.get(url);
		request.queryString("token", token );
		request.queryString("user", userId);
		request.queryString("scope", "read");

		Optional<JSONObject> respuestaJson = sendRequest(request);

		if (respuestaJson.isPresent()) {
			logger.info("Respuesta user info: {}", respuestaJson.get());
			return respuestaJson.get();
		} else {
			return null;
		}

	}


	private Optional<JSONObject> sendRequest(HttpRequest request) {
		try {
			if (request != null) {
				HttpResponse<JsonNode> response = request.asJson();

				if (response != null && response.getStatus() == HttpStatus.SC_OK) {
					return Optional.ofNullable(response.getBody().getObject());
				} else {
					logger.error("Response error for request {}: {}", request.getUrl(), response.getStatus());
				}
			}
		} catch (UnirestException e) {
			logger.error("There was an error trying to access instrument service with request {}: ",
					request.getUrl(), e);
		}

		return Optional.empty();
	}

	private class UserInfo {
		private String token;
		private String userId;

		public UserInfo(String token, String userId) {
			this.token = token;
			this.userId = userId;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}
	}


}
