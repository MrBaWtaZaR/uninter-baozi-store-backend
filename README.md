# Baozi Store

Trabalho prático de Desenvolvimento Web Back-End da UNINTER. API REST de uma loja fictícia de pãozinho chinês, com clientes, produtos e pedidos.

## Tecnologias
Java 17, Spring Boot, Spring MVC, Spring Data JPA, H2 e JSON.

## Organização
- `model`: Cliente, Produto e Pedido.
- `repository`: acesso ao banco.
- `controller`: requisições HTTP e respostas.
- `dto/PedidoDados`: IDs de cliente e produto no JSON.

## Como executar
Abra a pasta no VS Code. No PowerShell:

```powershell
java -version
.\mvnw.cmd spring-boot:run
```

O Maven Wrapper já está incluído. A API funciona em `http://localhost:8080`. Pare com Ctrl+C. O banco H2 é criado em `data/baozi-store.mv.db` e preserva os dados entre reinicializações.

Para executar o teste básico: `.\mvnw.cmd test`.

## Endpoints
Para cada um dos recursos `/clientes`, `/produtos` e `/pedidos`:
- `POST /recurso`: cadastrar.
- `GET /recurso`: listar.
- `GET /recurso/{id}`: consultar.
- `DELETE /recurso/{id}`: excluir.

## Exemplos de JSON para o Postman

Cliente (`POST /clientes`):
```json
{"nome":"NomeAluno123456","clienteDesde":"2026-10-12"}
```

Produto (`POST /produtos`):
```json
{"nome":"Baozi tradicional","preco":12.00,"estoque":true}
```

Pedido (`POST /pedidos`):
```json
{"clienteId":1,"produtoId":1,"quantidade":3}
```

Substitua os IDs pelo resultado real dos cadastros. Para excluir cliente ou produto com pedido vinculado, exclua primeiro o pedido.

## Entrega
Faltam os testes manuais e prints no Postman, o diagrama UML e o relatório PDF seguindo o modelo da faculdade.
