# Verificação técnica da Baozi Store

Verificação executada em 12/10/2026, no Windows, com Java Temurin 17.0.17, Spring Boot 3.5.16 e Maven Wrapper (Maven 3.10.0).

## Compilação e testes

Comandos realmente executados na raiz do projeto, usando um cache local ignorado pelo Git:

```powershell
.\mvnw.cmd -B -ntp '-Dmaven.repo.local=.m2' '-DskipTests' compile
.\mvnw.cmd -B -ntp '-Dmaven.repo.local=.m2' clean verify
.\mvnw.cmd -B -ntp '-Dmaven.repo.local=.m2' verify
```

A última execução, após a configuração que rejeita quantidades fracionárias, terminou com **BUILD SUCCESS** e:

```text
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
```

Foram 20 casos de API em `BaoziStoreApplicationTests` e um caso de persistência em `PersistenciaH2Tests`. Os relatórios detalhados estão em `target/surefire-reports/` na máquina em que os testes são executados; a pasta target não é publicada.

Os testes verificam as 12 operações, JSON do pedido com IDs, IDs automáticos, dados obrigatórios, nomes vazios, datas malformadas, limites de preço e quantidade, JSON malformado, IDs não numéricos, registros inexistentes, vários pedidos para um mesmo cliente/produto e exclusão protegida.

O teste de persistência gravou cliente, produto e pedido em um H2 de arquivo temporário, fechou normalmente o contexto Spring e o reabriu. Os três registros e seus relacionamentos continuaram disponíveis. Os testes não alteram o banco de uso local.

O JAR executável foi gerado em `target/baozi-store-0.0.1-SNAPSHOT.jar`.

## Servidor HTTP real

Foi executado:

```powershell
java -jar .\target\baozi-store-0.0.1-SNAPSHOT.jar --server.port=18080
```

A porta 18080 foi usada apenas na verificação; a aplicação continua configurada para 8080 por padrão. O servidor iniciou com Tomcat, encontrou três repositórios JPA e conectou-se a `jdbc:h2:file:./data/baozi-store`. O arquivo `data/baozi-store.mv.db` foi criado.

Requisições HTTP locais foram enviadas pelo PowerShell ao JAR em execução, usando dados fictícios:

| Verificação | Resultado real |
| --- | --- |
| POST /clientes, /produtos e /pedidos | 201 em cada rota |
| GET das três listagens | 200 em cada rota |
| GET por ID das três entidades criadas | 200 em cada rota |
| JSON do pedido | IDs do cliente/produto e quantidade conferidos |
| DELETE do cliente e produto vinculados | 409 em ambas as rotas |
| POST de pedido com quantidade 1.5 | 400 |
| POST de pedido com cliente inexistente | 404 |
| DELETE do pedido, seguido do cliente e produto | 204 em cada rota |
| GET por ID inexistente das três entidades | 404 em cada rota |
| GET /h2-console/ | 200 |

Ao final, os registros fictícios foram excluídos na ordem pedido, cliente e produto. O servidor foi interrompido com Ctrl+C; os logs confirmaram o encerramento normal do Tomcat, do EntityManagerFactory e do HikariPool.

A execução restrita do ambiente inicialmente bloqueou a conexão local do Tomcat. A mesma versão foi executada com a permissão de rede local concedida, sem alteração do código.

## Limites desta verificação

Esta é uma verificação técnica por testes automatizados e requisições HTTP locais. Os testes e as capturas manuais do Postman ainda devem ser feitos pelo aluno. Não foram produzidos diagrama UML, capturas do Postman ou PDF acadêmico.

O enunciado oficial em PDF e o modelo DOCX não foram encontrados na pasta da faculdade nem nos anexos disponíveis; a missão textual fornecida pelo aluno orientou a implementação. A conferência com esses documentos permanece pendente.
