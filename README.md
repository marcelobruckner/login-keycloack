# login-keycloack

API Quarkus com Keycloak (OIDC) e controle de acesso por roles.

## Requisitos

- Java 17
- Docker e Docker Compose (para subir Keycloak e app juntos)

## Executar em modo dev (local)

```bash
./mvnw quarkus:dev
```

Dev UI: `http://localhost:9090/q/dev/`

> O perfil `dev` usa o Keycloak em `http://localhost:8080`.

## Executar com Docker Compose

Sobe o Keycloak e o backend em rede interna:

```bash
docker compose up --build
```

- Backend: `http://localhost:9090`
- Keycloak: `http://localhost:8080`

Observações importantes:
- O backend usa o perfil `docker` (definido no `docker-compose.yml`).
- Não configure `KC_HOSTNAME=localhost`, pois o Keycloak publicará metadados OIDC apontando para `localhost` e isso quebra a comunicação entre containers.

## Endpoints

- `GET /hello/public` (sem autenticação)
- `GET /hello/private/admin` (role `admin`)
- `GET /hello/private/user` (role `user`)
- `GET /hello/private/viewer` (role `viewer`)
- `GET /hello/me` (autenticado, retorna JSON com usuário e roles)
- `GET /client-roles/private/read` (role `read`)
- `GET /client-roles/private/write` (role `write`)
- `GET /debug` (autenticado, retorna todas as claims do token)

### Exemplo de resposta do /hello/me

```json
{"username":"marcelo","roles":["admin"]}
```

## Keycloak (local)

Se quiser subir só o Keycloak via Docker:

```bash
docker run -d --name keycloak \
  -p 8080:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.5.6 start-dev
```

Admin Console: `http://localhost:8080` (usuário: `admin`, senha: `admin`).

### Criação inicial no Keycloak (resumo)

1. Criar o realm: `redhat-dev`.
2. Criar o client: `login-keycloak` (OIDC).
3. Criar usuários: `marcelo`, `ana`, `joao` (com senhas).
4. Criar roles do realm: `admin`, `user`, `viewer`.
5. Atribuir roles aos usuários:
   - `marcelo` -> `admin`
   - `ana` -> `user`
   - `joao` -> `viewer`
6. Criar roles do client: `read`, `write`.
7. Atribuir roles do client aos usuários conforme necessário.

## Obter token (password grant)

1. Habilite **Direct Access Grants** no client do Keycloak.
2. Solicite o token:

```bash
curl -s -X POST \
  http://localhost:8080/realms/redhat-dev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=login-keycloak" \
  -d "username=marcelo" \
  -d "password=marcelo" \
  -d "scope=openid roles"
```

3. Chame um endpoint protegido usando o `access_token`:

```bash
curl -s -H "Authorization: Bearer ACCESS_TOKEN" \
  http://localhost:9090/hello/private/admin
```

## Perfis de execução

- `web`: login via browser (OIDC web-app, com redirect e sessão)
- `api`: uso via REST API (OIDC service, com bearer token)

Para executar:

```bash
./mvnw quarkus:dev -Dquarkus.profile=web
```

```bash
./mvnw quarkus:dev -Dquarkus.profile=api
```

## OpenShift/CRC (deploy)

Manifestos em `k8s/`.

Observações importantes:
- Se estiver usando **Red Hat Single Sign-On 7.5**, o `auth-server-url` precisa conter `/auth`:
  - Exemplo: `https://sso-sso.apps-crc.testing/auth/realms/redhat-dev`
- Em CRC, o certificado é autoassinado. Para testes, use:
  - `QUARKUS_OIDC_TLS_VERIFICATION=none`
- Como a Route termina TLS na borda, habilite proxy forwarding:
  - `QUARKUS_HTTP_PROXY_PROXY_ADDRESS_FORWARDING=true`
  - `QUARKUS_HTTP_PROXY_ALLOW_FORWARDED=true`

Aplicar:

```bash
oc apply -f k8s/
```

## Empacotar e executar a aplicação

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Para gerar um _uber-jar_:

```bash
./mvnw package -Dquarkus.package.jar.type=uber-jar
java -jar target/*-runner.jar
```

## Executável nativo

```bash
./mvnw package -Dnative
```

Ou, se não tiver o GraalVM instalado:

```bash
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Depois, execute:

```bash
./target/login-keycloack-1.0.0-SNAPSHOT-runner
```
