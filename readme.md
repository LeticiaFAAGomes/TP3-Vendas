# 🛒 Sistema de Microsserviços — E-commerce

### Autenticação, Clientes, Produtos e Vendas com Spring Boot, Docker e Kubernetes

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)

Sistema distribuído de **e-commerce**, desenvolvido utilizando uma arquitetura baseada em **microsserviços**, com **Spring Boot**, **Spring Cloud (Eureka, Config Server e Gateway)**, autenticação via **JWT**, **Docker**, **Docker Compose** e **Kubernetes**.

O projeto aplica conceitos de microsserviços, service discovery, configuração centralizada, roteamento via API Gateway, autenticação e autorização, comunicação entre serviços, conteinerização e orquestração com Kubernetes.

---

## 📌 Objetivos

- Desenvolver uma aplicação utilizando arquitetura de microsserviços.
- Separar as responsabilidades do sistema em serviços independentes.
- Implementar autenticação e geração de tokens JWT.
- Centralizar as configurações dos microsserviços com Config Server.
- Registrar e descobrir serviços com Eureka Server.
- Rotear as requisições externas através de um API Gateway.
- Validar o token JWT nas requisições que passam pelo Gateway.
- Implementar comunicação entre microsserviços (vendas ↔ produtos).
- Criar imagens Docker para todos os microsserviços.
- Utilizar Docker Compose para executar os serviços em conjunto.
- Migrar a aplicação para Kubernetes.
- Utilizar Deployments e Services no Kubernetes para cada componente.

---

## 🛒 Sobre o Sistema

O sistema é dividido nos seguintes componentes:

- **eureka-server** — servidor de descoberta de serviços (Service Discovery).
- **config-server** — servidor de configuração centralizada, alimentado pelo `config-repo`.
- **gateway** — porta de entrada única da aplicação, responsável por rotear as requisições e validar o token JWT.
- **auth-service** — responsável pelo cadastro de usuários, login e geração/validação de tokens JWT.
- **clientes-service** — responsável pelo cadastro e gerenciamento de clientes.
- **produtos-service** — responsável pelo cadastro e gerenciamento de produtos.
- **vendas-service** — responsável pelo registro de vendas, consultando o `produtos-service` para validar os produtos vendidos.

Todas as requisições externas passam pelo **Gateway**, que consulta o **Eureka** para localizar as instâncias dos serviços e valida o token JWT antes de encaminhar a requisição ao microsserviço de destino.

---

## 🏛️ Arquitetura

```text
                                   ┌─────────────────────────┐
                                   │         CLIENTE         │
                                   │       REST / HTTP       │
                                   └────────────┬────────────┘
                                                │
                                                ▼
                                     ┌──────────────────────┐
                                     │       GATEWAY        │
                                     │   (TokenFilter/JWT)  │
                                     └──────────┬───────────┘
                                                │
                    ┌───────────────┬───────────┼───────────────┬───────────────┐
                    ▼               ▼           ▼               ▼               ▼
           ┌────────────────┐┌────────────┐┌────────────┐┌────────────┐┌────────────────┐
           │  auth-service  ││ clientes-  ││ produtos-  ││ vendas-    ││  eureka-server │
           │                ││ service    ││ service    ││ service    ││ config-server  │
           └───────┬────────┘└─────┬──────┘└─────┬──────┘└─────┬──────┘└────────────────┘
                   │               │             │             │
                   ▼               ▼             ▼             ▼
              ┌─────────┐    ┌──────────┐   ┌──────────┐   ┌─────────┐
              │   DB    │    │   DB     │   │    DB    │   │   DB    │
              │  Auth   │    │ Clientes │   │ Produtos │   │ Vendas  │
              └─────────┘    └──────────┘   └──────────┘   └─────────┘
```

O `vendas-service` consulta o `produtos-service` (via `ProdutoInterface`) para validar os produtos antes de registrar uma venda. Todos os serviços se registram no **Eureka** e buscam suas configurações no **Config Server**.

---

## 🧩 Componentes da Aplicação

| Componente           | Responsabilidade                                             |
| -------------------- | ------------------------------------------------------------ |
| **eureka-server**    | Registro e descoberta de serviços (Service Discovery)        |
| **config-server**    | Configuração centralizada dos microsserviços                 |
| **gateway**          | Roteamento das requisições e validação do token JWT          |
| **auth-service**     | Cadastro de usuários, login e geração/validação de token JWT |
| **clientes-service** | Gerenciamento de clientes                                    |
| **produtos-service** | Gerenciamento de produtos                                    |
| **vendas-service**   | Registro de vendas, com validação de produtos                |

---

# 🔐 Autenticação

A autenticação do sistema utiliza **JWT (JSON Web Token)**.

O responsável pela autenticação é o:

```text
auth-service
```

O serviço possui responsabilidade independente dos demais microsserviços.

### Fluxo de autenticação

```text
Usuário
   │
   │ login
   ▼
auth-service
   │
   │ valida credenciais
   ▼
JWT
   │
   │ Authorization: Bearer <token>
   ▼
Gateway
   │
   │ valida JWT
   ▼
Microsserviço protegido
```

As requisições que não possuem uma credencial válida são rejeitadas pelo Gateway com:

```text
401 Unauthorized
```

---

# 🔑 auth-service

O `auth-service` é responsável pela autenticação dos usuários.

### Responsabilidades

- Cadastrar usuários.
- Autenticar usuários.
- Validar e-mail e senha.
- Criptografar senhas utilizando BCrypt.
- Gerar tokens JWT.
- Disponibilizar o endpoint de login.
- Disponibilizar o endpoint de refresh.
- Persistir os usuários em seu próprio banco de dados.

### Estrutura

```text
auth-service/

└── src/main
    ├── java/com/exemplo/authservice
    │   ├── config
    │   │   └── SenhaConfig.java
    │   ├── controller
    │   │   └── UsuarioController.java
    │   ├── dto
    │   │   ├── LoginRequest.java
    │   │   ├── LoginResponse.java
    │   │   └── UsuarioRequest.java
    │   ├── model
    │   │   └── Usuario.java
    │   ├── repository
    │   │   └── UsuarioRepository.java
    │   ├── service
    │   │   ├── JwtToken.java
    │   │   └── UsuarioService.java
    │   └── AuthServiceApplication.java
    └── resources
        └── application.properties
```

---

# 🔓 Endpoints Públicos

As seguintes rotas não exigem token JWT:

| Método | Endpoint            | Descrição                        |
| ------ | ------------------- | -------------------------------- |
| POST   | `/usuarios`         | Cadastro de usuário              |
| POST   | `/usuarios/login`   | Autenticação e obtenção do token |
| POST   | `/usuarios/refresh` | Renovação do token               |

No Gateway, essas rotas devem ser liberadas para permitir que o usuário se cadastre, realize login e renove sua credencial.

---

# 🔒 Endpoints Protegidos

As rotas dos demais microsserviços exigem autenticação válida.

| Método | Endpoint         | Descrição            |
| ------ | ---------------- | -------------------- |
| GET    | `/clientes`      | Lista clientes       |
| GET    | `/produtos`      | Lista produtos       |
| GET    | `/produtos/{id}` | Busca produto por ID |
| GET    | `/vendas`        | Consulta vendas      |
| POST   | `/vendas`        | Registra uma venda   |

O token deve ser enviado no cabeçalho HTTP:

```http
Authorization: Bearer <token>
```

---

# 🔄 Refresh Token

O sistema deve disponibilizar uma rota para renovação da credencial:

```http
POST /usuarios/refresh
```

O objetivo do endpoint é permitir que o usuário obtenha uma nova credencial de acesso utilizando o mecanismo de refresh definido pela aplicação.

Fluxo esperado:

```text
Access Token
     │
     │ expira / precisa ser renovado
     ▼
POST /usuarios/refresh
     │
     ▼
Novo token de acesso
```

---

# 🚦 Proteção das Rotas

O Gateway possui um `TokenFilter` responsável por verificar as requisições.

O filtro:

1. Identifica a rota acessada.
2. Verifica se a rota é pública.
3. Caso seja protegida, procura o cabeçalho `Authorization`.
4. Verifica se o formato utiliza `Bearer`.
5. Valida a assinatura do JWT.
6. Libera a requisição quando o token é válido.
7. Retorna `401 Unauthorized` quando o token é ausente ou inválido.

### Fluxo

```text
Requisição
    │
    ▼
Gateway
    │
    ├── Rota pública ──────► encaminha
    │
    └── Rota protegida
            │
            ▼
       Possui JWT?
        │       │
       não     sim
        │       │
       401      ▼
             JWT válido?
              │      │
             não    sim
              │      │
             401    ▼
                 encaminha
```

---

# 👥 clientes-service

O `clientes-service` é responsável pelo gerenciamento dos **clientes**.

### Responsabilidades

- Listar clientes.
- Persistir os dados em seu próprio banco de dados.
- Popular dados iniciais através do `DataInitializer`.

### Endpoints

| Método | Endpoint    | Descrição               |
| ------ | ----------- | ----------------------- |
| GET    | `/clientes` | Lista todos os clientes |

---

# 📦 produtos-service

O `produtos-service` é responsável pelo gerenciamento dos **produtos**.

### Responsabilidades

- Listar produtos.
- Buscar produto por ID.
- Persistir os dados em seu próprio banco de dados.
- Popular dados iniciais através do `DataInitializer`.
- Responder às consultas feitas pelo `vendas-service`.

### Endpoints

| Método | Endpoint         | Descrição               |
| ------ | ---------------- | ----------------------- |
| GET    | `/produtos`      | Lista todos os produtos |
| GET    | `/produtos/{id}` | Busca um produto por ID |

---

# 💰 vendas-service

O `vendas-service` é responsável pelo registro das **vendas**, consultando o `produtos-service` para validar os produtos informados.

### Responsabilidades

- Registrar uma venda a partir do ID do produto e da quantidade.
- Consultar o `produtos-service` através do `ProdutoInterface` para validar o produto e obter seu valor.
- Persistir os dados das vendas em seu próprio banco de dados.

### Endpoints

| Método | Endpoint  | Descrição                                              |
| ------ | --------- | ------------------------------------------------------ |
| GET    | `/vendas` | Endpoint de verificação (retorna uma mensagem fixa)    |
| POST   | `/vendas` | Registra uma nova venda, validando o produto informado |

---

# 🔄 Comunicação entre Microsserviços

O `vendas-service` precisa consultar o `produtos-service` para verificar se o produto informado existe e obter seus dados.

Essa comunicação é realizada pelo `ProdutoInterface`, utilizando o nome do serviço registrado no Eureka:

```text
VENDAS-SERVICE
       │
       │ GET /produtos/{id}
       ▼
PRODUTOS-SERVICE
       │
       ▼
    Resposta
```

---

# 🧭 eureka-server

O `eureka-server` é o servidor de **Service Discovery** da aplicação.

Todos os microsserviços (`auth-service`, `clientes-service`, `produtos-service`, `vendas-service`, `gateway`, `config-server`) se registram no Eureka ao subir, permitindo que sejam localizados uns pelos outros apenas pelo nome.

```text
kubectl get pods -l app=eureka-server
```

---

# ⚙️ config-server

O `config-server` centraliza as configurações de todos os microsserviços, servindo os arquivos de propriedades armazenados no `config-repo`:

```text
config-repo/
├── auth-service.properties
├── auth-service-docker.properties
├── clientes-service.properties
├── clientes-service-docker.properties
├── produtos-service.properties
├── produtos-service-docker.properties
├── vendas-service.properties
└── vendas-service-docker.properties
```

Cada microsserviço possui um arquivo de configuração padrão e uma variação `-docker`, utilizada quando os serviços são executados em containers (ambiente onde os hosts dos bancos de dados e demais serviços mudam).

---

# 🚪 gateway

O `gateway` é o **ponto único de entrada** da aplicação, responsável por:

- Rotear as requisições para o microsserviço correto, utilizando o Eureka.
- Validar o token JWT das requisições através do `TokenFilter`, liberando o acesso apenas às rotas autorizadas (como login e cadastro de usuário).

```text
CLIENTE
   │
   ▼
GATEWAY (TokenFilter)
   │
   ├── válido    → encaminha para o microsserviço de destino
   └── inválido  → retorna 401 Unauthorized
```

---

# 🐳 Docker

Cada componente da aplicação (`auth-service`, `clientes-service`, `produtos-service`, `vendas-service`, `gateway`, `eureka-server`, `config-server`) possui seu próprio `Dockerfile`.

### Exemplo — auth-service

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Os demais serviços seguem a mesma estrutura, alterando apenas a porta exposta.

---

# 🐳 Docker Compose

O projeto possui um arquivo `docker-compose.yml` na raiz, responsável por orquestrar todos os componentes:

```text
eureka-server
config-server
gateway
auth-service
clientes-service
produtos-service
vendas-service
+ bancos de dados de cada serviço
```

### Executar

```bash
docker compose up -d
```

### Verificar os containers

```bash
docker compose ps
```

O Docker Compose garante que o `eureka-server` e o `config-server` subam antes dos demais microsserviços, já que todos dependem deles para se registrar e obter suas configurações.

---

# ☸️ Kubernetes

Após a validação com Docker e Docker Compose, a aplicação foi migrada para o Kubernetes.

Os manifestos estão organizados na pasta `k8s/`, aplicados em ordem, garantindo que a infraestrutura de suporte suba primeiro:

```text
k8s/
├── 00-namespace.yaml
├── 01-eureka-server.yaml
├── 02-config-server.yaml
├── 03-produtos-service.yaml
├── 04-vendas-service.yaml
├── 05-gateway.yaml
├── 06-clientes-service.yaml
├── 07-auth-service.yaml
└── README.md
```

### Aplicar os manifestos

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-eureka-server.yaml
kubectl apply -f k8s/02-config-server.yaml
kubectl apply -f k8s/03-produtos-service.yaml
kubectl apply -f k8s/04-vendas-service.yaml
kubectl apply -f k8s/05-gateway.yaml
kubectl apply -f k8s/06-clientes-service.yaml
kubectl apply -f k8s/07-auth-service.yaml
```

### Verificar os recursos

```bash
kubectl get pods -n <namespace>
kubectl get deployments -n <namespace>
kubectl get services -n <namespace>
```

Cada microsserviço foi configurado com um **Deployment** e um **Service**, permitindo réplicas e acesso interno estável dentro do cluster.

---

# 📁 Estrutura do Projeto

```text
.
├── auth-service/
│   ├── src/main
│   │   ├── java/com/exemplo/authservice
│   │   │   ├── config/SenhaConfig.java
│   │   │   ├── controller/UsuarioController.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   └── UsuarioRequest.java
│   │   │   ├── model/Usuario.java
│   │   │   ├── repository/UsuarioRepository.java
│   │   │   ├── service/
│   │   │   │   ├── JwtToken.java
│   │   │   │   └── UsuarioService.java
│   │   │   └── AuthServiceApplication.java
│   │   └── resources/application.properties
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── clientes-service/
│   ├── src/main
│   │   ├── java/com/exemplo/clientesservice
│   │   │   ├── config/DataInitializer.java
│   │   │   ├── controller/ClienteController.java
│   │   │   ├── model/Cliente.java
│   │   │   ├── repository/ClienteRepository.java
│   │   │   ├── service/ClienteService.java
│   │   │   └── ClienteServiceApplication.java
│   │   └── resources/application.properties
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── config-repo/
│   ├── auth-service.properties
│   ├── auth-service-docker.properties
│   ├── clientes-service.properties
│   ├── clientes-service-docker.properties
│   ├── produtos-service.properties
│   ├── produtos-service-docker.properties
│   ├── vendas-service.properties
│   └── vendas-service-docker.properties
│
├── config-server/
│   ├── src/main
│   │   ├── java/com/exemplo/configserver/ConfigServerApplication.java
│   │   └── resources/application.properties
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── eureka-server/
│   ├── src/main
│   │   ├── java/com/exemplo/eurekaserver/EurekaServerApplication.java
│   │   └── resources/application.properties
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── gateway/
│   ├── .mvn/wrapper
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example/gateway
│   │   │   │   ├── filter/TokenFilter.java
│   │   │   │   └── GatewayApplication.java
│   │   │   └── resources/application.properties
│   │   └── test/java/com/example/gateway
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── k8s/
│   ├── 00-namespace.yaml
│   ├── 01-eureka-server.yaml
│   ├── 02-config-server.yaml
│   ├── 03-produtos-service.yaml
│   ├── 04-vendas-service.yaml
│   ├── 05-gateway.yaml
│   ├── 06-clientes-service.yaml
│   ├── 07-auth-service.yaml
│   └── README.md
│
├── produtos-service/
│   ├── src/main
│   │   ├── java/com/exemplo/produtosservice
│   │   │   ├── config/DataInitializer.java
│   │   │   ├── controller/ProdutoController.java
│   │   │   ├── model/Produto.java
│   │   │   ├── repository/ProdutoRepository.java
│   │   │   ├── service/
│   │   │   └── ProdutosServiceApplication.java
│   │   └── resources/
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── vendas-service/
│   ├── .mvn/wrapper
│   ├── src
│   │   ├── main
│   │   │   ├── java/com/example/vendas_service
│   │   │   │   ├── controllers/VendaController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ProdutoDTO.java
│   │   │   │   │   └── VendaDTO.java
│   │   │   │   ├── interfaces/ProdutoInterface.java
│   │   │   │   ├── models/Venda.java
│   │   │   │   ├── repository/VendasRepository.java
│   │   │   │   ├── services/VendaService.java
│   │   │   │   └── VendasServiceApplication.java
│   │   │   └── resources/application.properties
│   │   └── test/java/com/example/vendas_service
│   ├── .dockerignore
│   ├── Dockerfile
│   └── pom.xml
│
├── .gitignore
└── docker-compose.yml
```

---

# 🛠️ Tecnologias

- **Java 21**
- **Spring Boot 3**
- **Spring Cloud Netflix Eureka**
- **Spring Cloud Config**
- **Spring Cloud Gateway**
- **Spring Data JPA**
- **Spring Security / JWT**
- **PostgreSQL**
- **Maven**
- **Docker**
- **Docker Compose**
- **Kubernetes**

---

# 🚀 Como Executar

## 1. Gerar os projetos

Em cada microsserviço, execute:

```bash
mvn clean package
```

---

## 2. Criar as imagens Docker

```bash
docker build -t eureka-server ./eureka-server
docker build -t config-server ./config-server
docker build -t gateway ./gateway
docker build -t auth-service ./auth-service
docker build -t clientes-service ./clientes-service
docker build -t produtos-service ./produtos-service
docker build -t vendas-service ./vendas-service
```

---

## 3. Executar com Docker Compose

Na raiz do projeto:

```bash
docker compose up -d
```

Verifique:

```bash
docker compose ps
```

---

## 4. Executar no Kubernetes

Com o cluster ativo, aplique os manifestos na ordem:

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-eureka-server.yaml
kubectl apply -f k8s/02-config-server.yaml
kubectl apply -f k8s/03-produtos-service.yaml
kubectl apply -f k8s/04-vendas-service.yaml
kubectl apply -f k8s/05-gateway.yaml
kubectl apply -f k8s/06-clientes-service.yaml
kubectl apply -f k8s/07-auth-service.yaml
```

Verifique os Pods, Deployments e Services:

```bash
kubectl get pods
kubectl get deployments
kubectl get services
```

---

# 📚 Conceitos Aplicados

- Arquitetura de microsserviços;
- Service Discovery com Eureka;
- Configuração centralizada com Config Server;
- API Gateway e roteamento de requisições;
- Autenticação e autorização com JWT;
- APIs REST;
- Comunicação entre microsserviços;
- Docker, Dockerfile, Docker Image e Docker Container;
- Docker Compose;
- Kubernetes, Pods, Deployments e Services;
- Separação de responsabilidades entre serviços.

---

# 👩‍💻 Autora

**Letícia Gomes**

Projeto desenvolvido para a disciplina de **Microsserviços e DevOps com Spring Boot e Spring Cloud** do bloco de **Desenvolvimento de Softwares Escaláveis**, aplicando conceitos de microsserviços, Spring Cloud, autenticação JWT, Docker, Docker Compose e Kubernetes.
