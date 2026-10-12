package com.baozi.store.repository;

import com.baozi.store.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

// O Spring Data JPA fornece as operações básicas do banco.
public interface ClienteRepository extends JpaRepository<Cliente, Long> { }
