package com.leandro.controllers.db;

import com.leandro.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository <Usuario, String> {
	Usuario findOneByUserName(String username);


}
