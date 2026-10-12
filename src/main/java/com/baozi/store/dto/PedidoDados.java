package com.baozi.store.dto;

import com.baozi.store.model.Pedido;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Transporta IDs no JSON sem expor os objetos e relacionamentos JPA.
public class PedidoDados {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "O clienteId é obrigatório.")
    @Positive(message = "O clienteId deve ser positivo.")
    private Long clienteId;

    @NotNull(message = "O produtoId é obrigatório.")
    @Positive(message = "O produtoId deve ser positivo.")
    private Long produtoId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantidade;

    public PedidoDados() { }

    public PedidoDados(Pedido pedido) {
        id = pedido.getId();
        clienteId = pedido.getCliente().getId();
        produtoId = pedido.getProduto().getId();
        quantidade = pedido.getQuantidade();
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
