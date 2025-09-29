# Apache Camel ViaCEP Application

Aplicação **Spring Boot 3 + Apache Camel 4 + OpenFeign** para consulta de endereços a partir do **ViaCEP**.  
A API foi documentada em **OpenAPI 3** e integrada ao **Swagger UI**.

---

## 📌 Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 3.2**
- **Apache Camel 4 (Rest DSL, Servlet, Jackson)**
- **Spring Cloud OpenFeign**
- **Swagger UI (SpringDoc)**
- **Lombok**
- **JUnit 5 + WireMock** (para testes)

---

## 🚀 Endpoints Principais

- `GET /api/cep/{cep}` → consulta um endereço por CEP.  
- `GET /api/endereco/{uf}/{cidade}/{logradouro}` → rota alternativa usando Camel para consulta de endereço.  
- **Documentação OpenAPI**:
  - Arquivo estático: [`/openapi.yaml`](src/main/resources/static/openapi.yaml)  
  - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## ⚙️ Como rodar localmente

### Pré-requisitos
- Java 21+
- Maven 3.9+

### Passos
```bash
# Build do projeto
mvn clean package

# Rodar a aplicação
mvn spring-boot:run
```

O serviço estará disponível em:
```
http://localhost:8080/api
```

---

## 🧪 Testes
O projeto contém testes unitários e de integração utilizando **JUnit 5** e **WireMock**.

```bash
mvn test
```

---

## 📄 Estrutura do Projeto
```
src/main/java/com/mrsalustiano/   # Código fonte principal
    ├── Application.java
    ├── model/Endereco.java
    ├── client/ViaCepClient.java
    ├── service/CepService.java
    └── route/CepRoute.java

src/main/resources/
    ├── application.yml            # Configurações
    ├── logback-spring.xml         # Configuração de logs
    └── static/openapi.yaml        # Contrato OpenAPI 3

src/test/java/com/mrsalustiano/    # Testes
```

---

## 📝 Logging
Logs configurados via **Logback**, exibidos apenas no console.  
- Pacote da aplicação (`com.mrsalustiano`) está em `DEBUG` para facilitar depuração.  
- Camel e Feign também configurados com nível `DEBUG` no `application.yml`.  

---

## 📚 Referências
- [Spring Boot](https://spring.io/projects/spring-boot)  
- [Apache Camel](https://camel.apache.org/)  
- [ViaCEP API](https://viacep.com.br/)  
- [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign)  
- [Swagger UI](https://swagger.io/tools/swagger-ui/)  
