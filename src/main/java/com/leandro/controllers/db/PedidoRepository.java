package com.leandro.controllers.db;

import com.leandro.model.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends MongoRepository <Pedido, String> {

	@Query("{ 'restaurante': {$eq: ?0}, 'date' : { $gt: ?1, $lt: ?2 } }")
	List<Pedido> findTodayPedidos(String restaurante, LocalDateTime from, LocalDateTime to);

	@Query("{ 'date' : { $gt: ?0, $lt: ?1 } }")
	List<Pedido> findToday(LocalDateTime from, LocalDateTime to);

}
