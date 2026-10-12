# Baozi Store

API REST acadêmica da disciplina **Desenvolvimento Web Back-End**, do Centro Universitário Internacional UNINTER. A loja fictícia Baozi Store vende pãozinho chinês e precisa cadastrar, consultar e excluir clientes, produtos e pedidos.

Cada pedido contém um cliente, um produto e uma quantidade positiva. Um cliente e um produto podem aparecer em vários pedidos.

## Tecnologias e requisitos

- Java 17 ou 21 (o projeto foi configurado e verificado com Java 17).
- Spring Boot 3.5.16, Spring MVC, Spring Data JPA e Bean Validation.
- Banco relacional H2 persistente em arquivo.
- Maven Wrapper incluído: não é necessário instalar Maven separadamente.
- VS Code com Extension Pack for Java; Postman para os testes manuais posteriores.

A base foi gerada pelo Spring Initializr e adaptada para Spring Boot 3.5, mantendo a estrutura Maven convencional. A primeira execução do Wrapper exige internet para baixar Maven e as dependências. Não é necessário instalar MySQL Server.

## Abrir e executar no Windows

No PowerShell, a partir da pasta que contém o projeto:

```powershell
cd "C:\FACULDADE UNINTER\Desenvolvimento Web - Backend\uninter-baozi-store-backend"
code .
java -version
.\mvnw.cmd spring-boot:run
```

Se a pasta foi clonada em outro local, ajuste o caminho de `cd`. No VS Code também é possível usar **Arquivo > Abrir Pasta** e executar o último comando no terminal integrado. Aguarde a mensagem `Started BaoziStoreApplication`.

A API fica em `http://localhost:8080` e aceita conexões somente da própria máquina. A rota raiz `/` não tem página: utilize as rotas da tabela abaixo.

Para interromper o servidor, pressione **Ctrl+C** no terminal em que ele está rodando. Para reiniciar, execute novamente `.\mvnw.cmd spring-boot:run` na mesma pasta. Os registros persistem.

Para compilar, executar os testes e gerar o JAR:

```powershell
.\mvnw.cmd clean verify
java -jar .\target\baozi-store-0.0.1-SNAPSHOT.jar
```

Execute apenas uma instância por vez usando o mesmo banco. Se a porta 8080 estiver ocupada, pare a instância anterior ou execute:

```powershell
.\mvnw.cmd '-Dspring-boot.run.arguments=--server.port=8081' spring-boot:run
```

Nesse caso, utilize `http://localhost:8081`. Se o Wrapper indicar que Java não foi encontrado, confira `java -version` e se `JAVA_HOME` aponta para a pasta do JDK, sem o sufixo `bin`.

## Organização e arquitetura MVC

```text
src/main/java/com/baozi/store/
├── BaoziStoreApplication.java
├── model/
│   ├── Cliente.java
│   ├── Produto.java
│   └── Pedido.java
├── repository/
│   ├── ClienteRepository.java
│   ├── ProdutoRepository.java
│   └── PedidoRepository.java
├── controller/
│   ├── ClienteController.java
│   ├── ProdutoController.java
│   ├── PedidoController.java
│   └── TratamentoErros.java
└── dto/
    └── PedidoDados.java

src/main/resources/application.properties
src/test/java/com/baozi/store/
├── BaoziStoreApplicationTests.java
└── PersistenciaH2Tests.java
```

O pacote `model` representa os dados e os relacionamentos; `repository` fornece acesso ao banco por Spring Data JPA; `controller` recebe requisições e devolve respostas HTTP. No MVC da API, os dados das respostas são representados em JSON pelos conversores do Spring. `PedidoDados` transporta os identificadores dos relacionamentos sem serializar os objetos JPA.

O código mantém as verificações diretamente nos controllers para facilitar o estudo. Não há uma camada de serviços separada porque as operações são pequenas.

## Banco H2 e relacionamentos

As configurações estão em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/baozi-store
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
```

O banco inicia junto com a aplicação, sem um servidor separado. Na primeira execução, o H2 cria `data/baozi-store.mv.db` e o Hibernate cria as tabelas. Nas próximas execuções, `update` preserva os dados e ajusta as tabelas quando necessário. O caminho é relativo à pasta de execução: inicie sempre na raiz deste projeto para acessar o mesmo arquivo.

As tabelas de clientes e produtos têm IDs automáticos. Em `Pedido`, os campos `cliente` e `produto` usam `@ManyToOne`; as colunas `cliente_id` e `produto_id` são chaves estrangeiras obrigatórias. Não há exclusão em cascata.

O console local fica em `http://localhost:8080/h2-console`. Para conectar, use:

| Campo | Valor |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/baozi-store` |
| User Name | `sa` |
| Password | deixe vazio |

Esses são parâmetros do banco local de estudo, sem credenciais pessoais. O console só fica disponível enquanto a aplicação está rodando. Os arquivos do H2 são ignorados pelo Git.

## Endpoints

Todos os cadastros recebem JSON. IDs são gerados pelo banco; não envie o campo `id` no POST. O cabeçalho `Location` de uma criação indica a rota do registro criado.

| Método | Rota | Operação | Sucesso |
| --- | --- | --- | --- |
| POST | `/clientes` | Cadastrar cliente | 201 |
| GET | `/clientes` | Listar clientes | 200 |
| GET | `/clientes/{id}` | Consultar cliente | 200 |
| DELETE | `/clientes/{id}` | Excluir cliente | 204 |
| POST | `/produtos` | Cadastrar produto | 201 |
| GET | `/produtos` | Listar produtos | 200 |
| GET | `/produtos/{id}` | Consultar produto | 200 |
| DELETE | `/produtos/{id}` | Excluir produto | 204 |
| POST | `/pedidos` | Cadastrar pedido | 201 |
| GET | `/pedidos` | Listar pedidos | 200 |
| GET | `/pedidos/{id}` | Consultar pedido | 200 |
| DELETE | `/pedidos/{id}` | Excluir pedido | 204 |

Não há atualização por PUT nesta etapa.

## Exemplos de requisições e teste posterior no Postman

Os corpos abaixo são **exemplos para envio**, não resultados de testes manuais já realizados.

1. Com a aplicação iniciada, crie uma requisição no Postman.
2. Selecione o método POST, informe a URL completa e escolha **Body > raw > JSON**. O cabeçalho deve ser `Content-Type: application/json`.
3. Envie um cliente e anote o `id` realmente retornado.
4. Envie um produto e anote o `id` realmente retornado.
5. Cadastre um pedido com esses dois IDs.
6. Consulte cada entidade por ID e pelas listagens.
7. Tente excluir o cliente ou produto vinculado, observando o status 409.
8. Exclua primeiro o pedido e depois o cliente e o produto, observando 204.
9. Teste IDs inexistentes e entradas inválidas; registre as respostas reais e faça as capturas exigidas pela atividade.

**POST http://localhost:8080/clientes**

```json
{
  "nome": "Ana Silva",
  "clienteDesde": "2026-10-12"
}
```

**POST http://localhost:8080/produtos**

```json
{
  "nome": "Baozi de legumes",
  "preco": 12.50,
  "estoque": true
}
```

**POST http://localhost:8080/pedidos**

Substitua `1` pelos IDs efetivamente recebidos nos cadastros anteriores.

```json
{
  "clienteId": 1,
  "produtoId": 1,
  "quantidade": 2
}
```

O JSON de pedido usa `id`, `clienteId`, `produtoId` e `quantidade`, sem objetos aninhados nem referências recursivas. O preço é numérico, com ponto decimal. A data usa `AAAA-MM-DD`, e `estoque` deve ser um Boolean: `true` ou `false`.

## Validações e respostas de erro

- Nomes obrigatórios, sem apenas espaços, com até 120 caracteres.
- `clienteDesde` obrigatório, como data válida.
- Preço obrigatório e positivo, com até 8 dígitos inteiros e 2 decimais.
- `estoque` obrigatório; representa disponibilidade, não quantidade. Esta etapa não adiciona uma regra que bloqueie pedidos pela disponibilidade.
- IDs relacionados obrigatórios e positivos; cliente e produto devem existir.
- Quantidade obrigatória, inteira, positiva e dentro do limite de Integer.
- IDs recebidos no POST são ignorados: o cadastro sempre cria um novo registro.

Entradas inválidas retornam **400**. Registros inexistentes retornam **404**, inclusive um cliente ou produto inexistente ao cadastrar um pedido. Excluir cliente ou produto com pedidos vinculados retorna **409**, preservando todos os registros. Para excluir, remova primeiro os pedidos vinculados. Uma exclusão bem-sucedida retorna **204** com corpo vazio. Uma listagem sem registros retorna **200** com `[]`.

Os erros previstos retornam JSON com a propriedade `mensagem`, em português.

## Testes automatizados

```powershell
.\mvnw.cmd test
```

`BaoziStoreApplicationTests` usa Spring Boot, MockMvc, JPA e um H2 em memória isolado, cobrindo as 12 operações, códigos HTTP, validações, IDs automáticos, formato do JSON, vários pedidos para o mesmo cliente/produto e proteção dos vínculos.

`PersistenciaH2Tests` usa um arquivo temporário, salva as três entidades, fecha normalmente o contexto Spring e o reabre para consultar os registros e os relacionamentos. Esse teste verifica a persistência sem alterar o banco de uso local.

Os resultados reais da verificação técnica estão em `VERIFICACAO.md`. Esses testes não substituem a execução manual nem as capturas do Postman.

## Etapas acadêmicas pendentes

Conferir o projeto com o enunciado oficial e o modelo da atividade, executar os testes manuais do Postman com capturas reais, elaborar o diagrama UML exigido e produzir o PDF acadêmico final. Os documentos oficiais não estavam disponíveis na pasta do projeto durante a implementação; o código foi construído a partir da missão fornecida pelo aluno.

## Referências

- [Requisitos do Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring Initializr](https://start.spring.io/)
- [H2 Database](https://h2database.com/html/main.html)
