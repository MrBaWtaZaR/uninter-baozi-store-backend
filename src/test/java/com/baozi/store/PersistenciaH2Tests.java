package com.baozi.store;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import com.baozi.store.model.*;
import com.baozi.store.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.*;

// Fecha e reabre a aplicação para verificar a persistência real em arquivo.
class PersistenciaH2Tests {
    @TempDir
    Path pastaTemporaria;

    @Test
    void manterRegistrosERelacionamentosAposReinicializacaoNormal() {
        String url = "jdbc:h2:file:" + pastaTemporaria.resolve("baozi").toString().replace('\\', '/');
        Long pedidoId;

        try (ConfigurableApplicationContext contexto = iniciar(url)) {
            Cliente cliente = new Cliente();
            cliente.setNome("Cliente de teste");
            cliente.setClienteDesde(LocalDate.of(2026, 10, 12));
            cliente = contexto.getBean(ClienteRepository.class).save(cliente);

            Produto produto = new Produto();
            produto.setNome("Baozi de teste");
            produto.setPreco(new BigDecimal("9.50"));
            produto.setEstoque(true);
            produto = contexto.getBean(ProdutoRepository.class).save(produto);

            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setProduto(produto);
            pedido.setQuantidade(2);
            pedidoId = contexto.getBean(PedidoRepository.class).save(pedido).getId();
        }

        try (ConfigurableApplicationContext contexto = iniciar(url)) {
            assertEquals(1, contexto.getBean(ClienteRepository.class).count());
            assertEquals(1, contexto.getBean(ProdutoRepository.class).count());
            assertEquals(1, contexto.getBean(PedidoRepository.class).count());
            // Mantém a transação aberta para ler os relacionamentos lazy.
            TransactionTemplate transacao = new TransactionTemplate(
                    contexto.getBean(PlatformTransactionManager.class));
            transacao.executeWithoutResult(status -> {
                Pedido pedido = contexto.getBean(PedidoRepository.class).findById(pedidoId).orElseThrow();
                assertEquals("Cliente de teste", pedido.getCliente().getNome());
                assertEquals("Baozi de teste", pedido.getProduto().getNome());
                assertEquals(2, pedido.getQuantidade());
            });
        }
    }

    private ConfigurableApplicationContext iniciar(String url) {
        return new SpringApplicationBuilder(BaoziStoreApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + url, "--spring.jpa.hibernate.ddl-auto=update",
                        "--spring.h2.console.enabled=false", "--spring.main.banner-mode=off");
    }
}
