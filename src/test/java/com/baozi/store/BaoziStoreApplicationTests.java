package com.baozi.store;

import com.baozi.store.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Executa as rotas MVC com JPA e H2 reais, sem alterar o banco do aluno.
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:baozi-testes;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class BaoziStoreApplicationTests {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private ClienteRepository clientes;
    @Autowired private ProdutoRepository produtos;
    @Autowired private PedidoRepository pedidos;

    @BeforeEach
    void limparBancoDeTeste() {
        pedidos.deleteAll();
        clientes.deleteAll();
        produtos.deleteAll();
    }

    @Test
    void cadastrarConsultarListarEExcluirAsTresEntidades() throws Exception {
        long clienteId = cadastrar("/clientes", """
                {"nome":"Ana","clienteDesde":"2026-10-12"}
                """);
        long produtoId = cadastrar("/produtos", """
                {"nome":"Baozi de legumes","preco":12.50,"estoque":true}
                """);
        long pedidoId = cadastrar("/pedidos", pedidoJson(clienteId, produtoId, 2));

        mvc.perform(get("/clientes/" + clienteId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.clienteDesde").value("2026-10-12"));
        mvc.perform(get("/produtos/" + produtoId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(12.50))
                .andExpect(jsonPath("$.estoque").value(true));
        mvc.perform(get("/pedidos/" + pedidoId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").value(clienteId))
                .andExpect(jsonPath("$.produtoId").value(produtoId))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.cliente").doesNotExist())
                .andExpect(jsonPath("$.produto").doesNotExist());

        for (String rota : new String[]{"/clientes", "/produtos", "/pedidos"}) {
            mvc.perform(get(rota)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        }

        mvc.perform(delete("/pedidos/" + pedidoId)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isNoContent());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isNoContent());
        assertEquals(0, pedidos.count());
        assertEquals(0, clientes.count());
        assertEquals(0, produtos.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/clientes", "/produtos", "/pedidos"})
    void registrosInexistentesRetornam404(String rota) throws Exception {
        mvc.perform(get(rota + "/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        mvc.perform(delete(rota + "/999999")).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/clientes", "/produtos", "/pedidos"})
    void listasVaziasRetornam200(String rota) throws Exception {
        mvc.perform(get(rota)).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/clientes", "/produtos", "/pedidos"})
    void camposObrigatoriosRetornam400(String rota) throws Exception {
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void rejeitarNomeVazioDataInvalidaEPrecoInvalido() throws Exception {
        verificarInvalido("/clientes", """
                {"nome":" ","clienteDesde":"2026-10-12"}
                """);
        verificarInvalido("/clientes", """
                {"nome":"Ana","clienteDesde":"data"}
                """);
        verificarInvalido("/produtos", """
                {"nome":"Baozi","preco":-1,"estoque":true}
                """);
        verificarInvalido("/produtos", """
                {"nome":"Baozi","preco":1.234,"estoque":true}
                """);
        verificarInvalido("/produtos", """
                {"nome":"Baozi","preco":100000000,"estoque":true}
                """);
        assertEquals(0, clientes.count());
        assertEquals(0, produtos.count());
    }

    @Test
    void rejeitarPedidoSemRelacionamentosExistentes() throws Exception {
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson(999999L, 999999L, 1))).andExpect(status().isNotFound());
        long clienteId = cadastrar("/clientes", """
                {"nome":"Ana","clienteDesde":"2026-10-12"}
                """);
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
                .content(pedidoJson(clienteId, 999999L, 1))).andExpect(status().isNotFound());
        assertEquals(0, pedidos.count());
    }

    @Test
    void rejeitarQuantidadesNaoPositivasEIdsInvalidos() throws Exception {
        verificarInvalido("/pedidos", pedidoJson(1, 1, 0));
        verificarInvalido("/pedidos", pedidoJson(1, 1, -2));
        verificarInvalido("/pedidos", pedidoJson(0, 1, 1));
        assertEquals(0, pedidos.count());
    }

    @Test
    void rejeitarQuantidadeFracionariaOuForaDoLimiteDeInteger() throws Exception {
        verificarInvalido("/pedidos", """
                {"clienteId":1,"produtoId":1,"quantidade":1.5}
                """);
        verificarInvalido("/pedidos", """
                {"clienteId":1,"produtoId":1,"quantidade":2147483648}
                """);
        assertEquals(0, pedidos.count());
    }

    @Test
    void protegerClienteEProdutoComPedidosVinculados() throws Exception {
        long clienteId = cadastrar("/clientes", """
                {"nome":"Ana","clienteDesde":"2026-10-12"}
                """);
        long produtoId = cadastrar("/produtos", """
                {"nome":"Baozi","preco":8.50,"estoque":false}
                """);
        long pedidoId = cadastrar("/pedidos", pedidoJson(clienteId, produtoId, 1));
        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isConflict());
        assertEquals(1, clientes.count());
        assertEquals(1, produtos.count());
        assertEquals(1, pedidos.count());
        mvc.perform(delete("/pedidos/" + pedidoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isNoContent());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isNoContent());
    }

    @Test
    void permitirVariosPedidosParaMesmoClienteEProduto() throws Exception {
        long clienteId = cadastrar("/clientes", """
                {"nome":"Ana","clienteDesde":"2026-10-12"}
                """);
        long produtoId = cadastrar("/produtos", """
                {"nome":"Baozi","preco":8.50,"estoque":true}
                """);
        cadastrar("/pedidos", pedidoJson(clienteId, produtoId, 1));
        cadastrar("/pedidos", pedidoJson(clienteId, produtoId, 3));
        assertEquals(2, pedidos.count());
        assertEquals(1, clientes.count());
        assertEquals(1, produtos.count());
    }

    @Test
    void ignorarIdEnviadoNoCadastroEManterRegistroExistente() throws Exception {
        long clienteId = cadastrar("/clientes", """
                {"nome":"Ana","clienteDesde":"2026-10-12"}
                """);
        long outroId = cadastrar("/clientes", "{\"id\":" + clienteId
                + ",\"nome\":\"Bruno\",\"clienteDesde\":\"2026-10-12\"}");
        assertNotEquals(clienteId, outroId);
        assertEquals(2, clientes.count());
        mvc.perform(get("/clientes/" + clienteId)).andExpect(jsonPath("$.nome").value("Ana"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/clientes", "/produtos", "/pedidos"})
    void jsonMalformadoEIdNaoNumericoRetornam400(String rota) throws Exception {
        verificarInvalido(rota, "{");
        mvc.perform(get(rota + "/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    private long cadastrar(String rota, String corpo) throws Exception {
        var resultado = mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated()).andReturn();
        JsonNode resposta = json.readTree(resultado.getResponse().getContentAsString());
        long id = resposta.get("id").asLong();
        assertTrue(id > 0);
        assertEquals(rota + "/" + id, resultado.getResponse().getHeader("Location"));
        return id;
    }

    private void verificarInvalido(String rota, String corpo) throws Exception {
        mvc.perform(post(rota).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    private String pedidoJson(long clienteId, long produtoId, int quantidade) {
        return "{\"clienteId\":" + clienteId + ",\"produtoId\":" + produtoId
                + ",\"quantidade\":" + quantidade + "}";
    }
}
