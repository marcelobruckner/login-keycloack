# login-keycloack

Este projeto utiliza o Quarkus, o framework Java Supersonic Subatomic.

Para saber mais sobre o Quarkus, visite: <https://quarkus.io/>.

## Executar a aplicacao em modo dev

Voce pode executar a aplicacao em modo dev (com live coding) usando:

```shell script
./mvnw quarkus:dev
```

> **_NOTA:_** O Quarkus inclui um Dev UI disponivel apenas em modo dev em <http://localhost:8080/q/dev/>.

## Empacotar e executar a aplicacao

A aplicacao pode ser empacotada com:

```shell script
./mvnw package
```

Isso gera o arquivo `quarkus-run.jar` no diretorio `target/quarkus-app/`.
Note que nao eh um _uber-jar_, pois as dependencias sao copiadas para `target/quarkus-app/lib/`.

A aplicacao pode ser executada com:

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Se voce quiser gerar um _uber-jar_, execute:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

O _uber-jar_ pode ser executado com:

```shell script
java -jar target/*-runner.jar
```

## Criar executavel nativo

Voce pode criar um executavel nativo usando:

```shell script
./mvnw package -Dnative
```

Ou, se nao tiver o GraalVM instalado, pode gerar a build nativa via container:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Depois, execute com:

```
./target/login-keycloack-1.0.0-SNAPSHOT-runner
```

Para saber mais sobre executaveis nativos, consulte <https://quarkus.io/guides/maven-tooling>.

## Autenticacao e Endpoints

Este projeto usa Keycloak (OIDC) e aplica controle de acesso por roles.

### Subir o Keycloak com Docker

```bash
docker run -d --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.5.5 start-dev
```

Admin Console: `http://localhost:8080` (usuario: `admin`, senha: `admin`).

### Criacao inicial no Keycloak (resumo)

1. Criar o realm: `redhat-dev`.
2. Criar o client: `login-keycloak` (OIDC).
3. Criar usuarios: `marcelo`, `ana`, `joao` (com senhas).
4. Criar roles do realm: `admin`, `user`, `viewer`.
5. Atribuir roles aos usuarios:
   - `marcelo` -> `admin`
   - `ana` -> `user`
   - `joao` -> `viewer`

### Endpoints

- `GET /hello/public` (sem autenticacao)
- `GET /hello/private/admin` (role `admin`)
- `GET /hello/private/user` (role `user`)
- `GET /hello/private/viewer` (role `viewer`)
- `GET /hello/me` (autenticado, retorna JSON com usuario e roles)

### Exemplo de resposta do /hello/me

```json
{"username":"marcelo","roles":["admin"]}
```

### Obter token (password grant)

1. Habilite **Direct Access Grants** no client do Keycloak.
2. Solicite o token:

```bash
curl -s -X POST \
  http://localhost:8080/realms/redhat-dev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=login-keycloak" \
  -d "client_secret=YOUR_SECRET" \
  -d "username=marcelo" \
  -d "password=marcelo" \
  -d "scope=openid roles"
```

3. Chame um endpoint protegido usando o `access_token`:

```bash
curl -s -H "Authorization: Bearer ACCESS_TOKEN" \
  http://localhost:9090/hello/private/admin
```

### Perfis de execucao (browser vs API)

Este projeto usa perfis do Quarkus para alternar o modo de autenticacao:

- `web`: login via browser (OIDC web-app, com redirect e sessao)
- `api`: uso via REST API (OIDC service, com bearer token)

Use `web` quando o acesso for via navegador e voce quiser redirecionamento automatico para login.
Use `api` quando o acesso for via clientes HTTP (Insomnia, curl, outros servicos) e o token vier no header.

Para executar:

```bash
./mvnw quarkus:dev -Dquarkus.profile=web
```

```bash
./mvnw quarkus:dev -Dquarkus.profile=api
```

## Guias relacionados

- REST ([guia](https://quarkus.io/guides/rest)): Implementacao Jakarta REST com processamento em build time e Vert.x. Essa extensao nao eh compativel com `quarkus-resteasy` nem com extensoes que dependem dela.

## Codigo fornecido

### REST

Inicie rapidamente seus servicos REST.

[Secao relacionada no guia](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
