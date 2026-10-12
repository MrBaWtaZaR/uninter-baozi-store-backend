package com.baozi.store.controller;

import java.net.URI;
import java.util.List;
import com.baozi.store.dto.PedidoDados;
import com.baozi.store.model.Cliente;
import com.baozi.store.model.Pedido;
import com.baozi.store.model.Produto;
import com.baozi.store.repository.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
                            ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PedidoDados> cadastrar(@Valid @RequestBody PedidoDados dados) {
        // Somente cria o pedido quando os dois registros relacionados existem.
        Cliente cliente = clienteRepository.findById(dados.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado."));
        Produto produto = produtoRepository.findById(dados.getProdutoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado."));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(dados.getQuantidade());
        Pedido salvo = pedidoRepository.saveAndFlush(pedido);
        return ResponseEntity.created(URI.create("/pedidos/" + salvo.getId())).body(new PedidoDados(salvo));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoDados> listar() {
        return pedidoRepository.findAll().stream().map(PedidoDados::new).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoDados buscar(@PathVariable Long id) {
        return new PedidoDados(buscarPedido(id));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        pedidoRepository.delete(buscarPedido(id));
        pedidoRepository.flush();
        return ResponseEntity.noContent().build();
    }

    private Pedido buscarPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado."));
    }
}
