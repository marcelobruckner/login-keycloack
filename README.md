# login-keycloack

Este projeto utiliza o Quarkus, o framework Java Supersonic Subatomic.

Para saber mais sobre o Quarkus, visite: <https://quarkus.io/>.

## Executar a aplicação em modo dev

Você pode executar a aplicação em modo dev (com live coding) usando:

```shell script
./mvnw quarkus:dev
```

> **_NOTA:_** O Quarkus inclui um Dev UI disponível apenas em modo dev em <http://localhost:9090/q/dev/>.

## Empacotar e executar a aplicação

A aplicação pode ser empacotada com:

```shell script
./mvnw package
```

Isso gera o arquivo `quarkus-run.jar` no diretório `target/quarkus-app/`.
Note que não é um _uber-jar_, pois as dependências são copiadas para `target/quarkus-app/lib/`.

A aplicação pode ser executada com:

```shell script
java -jar target/quarkus-app/quarkus-run.jar
```

Se você quiser gerar um _uber-jar_, execute:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

O _uber-jar_ pode ser executado com:

```shell script
java -jar target/*-runner.jar
```

## Criar executável nativo

Você pode criar um executável nativo usando:

```shell script
./mvnw package -Dnative
```

Ou, se não tiver o GraalVM instalado, pode gerar a build nativa via container:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

Depois, execute com:

```
./target/login-keycloack-1.0.0-SNAPSHOT-runner
```

Para saber mais sobre executáveis nativos, consulte <https://quarkus.io/guides/maven-tooling>.

## Autenticação e Endpoints

Este projeto usa Keycloak (OIDC) e aplica controle de acesso por roles.

### Subir o Keycloak com Docker

```bash
docker run -d --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.5.5 start-dev
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

### Endpoints

- `GET /hello/public` (sem autenticação)
- `GET /hello/private/admin` (role `admin`)
- `GET /hello/private/user` (role `user`)
- `GET /hello/private/viewer` (role `viewer`)
- `GET /hello/me` (autenticado, retorna JSON com usuário e roles)

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

### Perfis de execução (browser vs API)

Este projeto usa perfis do Quarkus para alternar o modo de autenticação:

- `web`: login via browser (OIDC web-app, com redirect e sessão)
- `api`: uso via REST API (OIDC service, com bearer token)

Use `web` quando o acesso for via navegador e você quiser redirecionamento automático para login.
Use `api` quando o acesso for via clientes HTTP (Insomnia, curl, outros serviços) e o token vier no header.

Para executar:

```bash
./mvnw quarkus:dev -Dquarkus.profile=web
```

```bash
./mvnw quarkus:dev -Dquarkus.profile=api
```

## OpenShift/CRC (deploy)

Esta seção descreve o deploy no OpenShift (CRC) usando os manifestos da pasta `k8s/`.

### Observações importantes

- Se estiver usando **Red Hat Single Sign-On 7.5**, o `auth-server-url` precisa conter `/auth`:
  - Exemplo: `https://sso-sso.apps-crc.testing/auth/realms/redhat-dev`
- Em CRC, o certificado é autoassinado. Para testes, usamos:
  - `QUARKUS_OIDC_TLS_VERIFICATION=none`
- Como a Route termina TLS na borda, habilite proxy forwarding:
  - `QUARKUS_HTTP_PROXY_PROXY_ADDRESS_FORWARDING=true`
  - `QUARKUS_HTTP_PROXY_ALLOW_FORWARDED=true`

### Manifestos

Os arquivos já estão em `k8s/`:

- `k8s/01-configmap.yaml`
- `k8s/02-secret.yaml`
- `k8s/03-deployment.yaml`
- `k8s/04-service.yaml`
- `k8s/05-route.yaml`

Aplicar:

```bash
oc apply -f k8s/
```

Depois, pegue a route da aplicação:

```bash
oc get route -n app
```

E configure o client no Keycloak:

- **Valid Redirect URIs**: `https://<route-da-app>/*`
- **Web Origins**: `https://<route-da-app>`

## Guias relacionados

- REST ([guia](https://quarkus.io/guides/rest)): Implementação Jakarta REST com processamento em build time e Vert.x. Essa extensão não é compatível com `quarkus-resteasy` nem com extensões que dependem dela.

## Código fornecido

### REST

Inicie rapidamente seus serviços REST.

[Seção relacionada no guia](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
