package com.baozi.store.repository;

import com.baozi.store.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring Data JPA fornece as operações básicas do banco.
public interface ProdutoRepository extends JpaRepository<Produto, Long> { }
