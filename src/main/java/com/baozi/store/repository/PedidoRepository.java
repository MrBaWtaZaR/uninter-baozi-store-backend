package com.baozi.store.repository;

import com.baozi.store.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring Data JPA fornece as operações básicas do banco.
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Verificam vínculos antes da exclusão de um cliente ou produto.
    boolean existsByClienteId(Long clienteId);
    boolean existsByProdutoId(Long produtoId);
}
