package com.baozi.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Verifica as operações básicas da API usando um banco de teste.
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:baozi-teste;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class BaoziStoreApplicationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Test
    void testarCadastroConsultaListagemEExclusao() throws Exception {
        String cliente = mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Aluno\",\"clienteDesde\":\"2026-10-12\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long clienteId = json.readTree(cliente).get("id").asLong();

        String produto = mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Baozi\",\"preco\":12.00,\"estoque\":true}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long produtoId = json.readTree(produto).get("id").asLong();

        String corpo = "{\"clienteId\":" + clienteId + ",\"produtoId\":" + produtoId + ",\"quantidade\":2}";
        String pedido = mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(corpo)).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long pedidoId = json.readTree(pedido).get("id").asLong();

        mvc.perform(get("/clientes/" + clienteId)).andExpect(status().isOk());
        mvc.perform(get("/produtos/" + produtoId)).andExpect(status().isOk());
        mvc.perform(get("/pedidos/" + pedidoId)).andExpect(status().isOk());
        mvc.perform(get("/clientes")).andExpect(status().isOk());
        mvc.perform(get("/produtos")).andExpect(status().isOk());
        mvc.perform(get("/pedidos")).andExpect(status().isOk());

        // Os pedidos precisam ser removidos antes dos registros relacionados.
        mvc.perform(delete("/pedidos/" + pedidoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isNoContent());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isNoContent());
    }
}
