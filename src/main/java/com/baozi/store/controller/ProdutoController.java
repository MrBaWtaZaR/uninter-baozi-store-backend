package com.baozi.store.controller;

import java.net.URI;
import java.util.List;
import com.baozi.store.model.Produto;
import com.baozi.store.repository.ProdutoRepository;
import com.baozi.store.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// Recebe as requisições HTTP e usa os repositórios para acessar os dados.
@RestController
@RequestMapping("/produtos")
public class ProdutoController {
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public ProdutoController(ProdutoRepository produtoRepository, PedidoRepository pedidoRepository) {
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody Produto produto) {
        Produto salvo = produtoRepository.save(produto);
        return ResponseEntity.created(URI.create("/produtos/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    public List<Produto> listar() {
        return produtoRepository.findAll();
    }

    @GetMapping("/{id}")
    public Produto buscar(@PathVariable Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        Produto produto = buscar(id);
        // Preserva os pedidos: o aluno deve excluí-los antes de remover o produto.
        if (pedidoRepository.existsByProdutoId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir produto com pedidos vinculados.");
        }
        produtoRepository.delete(produto);
        produtoRepository.flush();
        return ResponseEntity.noContent().build();
    }
}
