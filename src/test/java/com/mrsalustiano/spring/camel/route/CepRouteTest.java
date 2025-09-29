package com.mrsalustiano.spring.camel.route;

import com.mrsalustiano.spring.camel.model.Endereco;
import com.mrsalustiano.spring.camel.service.CepService;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@CamelSpringBootTest
@SpringBootTest
@TestPropertySource(properties = {
        "camel.springboot.java-routes-include-pattern=**/CepRoute"
})
@DisplayName("CepRoute - Testes de Integração")
public class CepRouteTest {
    @Autowired
    private ProducerTemplate producerTemplate;

    @MockBean
    private CepService cepService;

    private Endereco enderecoValido;

    @BeforeEach
    void setUp() throws Exception {
        enderecoValido = new Endereco();
        enderecoValido.setCep("01001-000");
        enderecoValido.setLogradouro("Praça da Sé");
        enderecoValido.setBairro("Sé");
        enderecoValido.setLocalidade("São Paulo");
        enderecoValido.setUf("SP");

    }

    @Test
    @DisplayName("Deve consultar CEP com sucesso")
    void deveConsultarCepComSucesso() throws Exception {
        // Given
        String cep = "01001000";
        when(cepService.buscarEnderecoPorCep(cep)).thenReturn(enderecoValido);

        // When
        Exchange exchange = producerTemplate.request("direct:consultarCep", processor -> {
            processor.getIn().setHeader("cep", cep);
        });

        // Then
        assertNotNull(exchange);
        assertNotNull(exchange.getMessage().getBody());
        assertEquals(enderecoValido, exchange.getMessage().getBody());
        verify(cepService, times(1)).buscarEnderecoPorCep(cep);
    }

    @Test
    @DisplayName("Deve retornar erro 400 para CEP inválido")
    void deveRetornarErro400ParaCepInvalido() throws Exception {
        // Given
        String cepInvalido = "123";
        when(cepService.buscarEnderecoPorCep(cepInvalido))
                .thenThrow(new IllegalArgumentException("CEP deve conter exatamente 8 dígitos"));

        // When
        Exchange exchange = producerTemplate.request("direct:consultarCep", processor -> {
            processor.getIn().setHeader("cep", cepInvalido);
        });

        // Then
        assertNotNull(exchange);
        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        assertEquals(400, statusCode);
        String body = exchange.getMessage().getBody(String.class);
        assertTrue(body.contains("CEP deve conter exatamente 8 dígitos"));
        verify(cepService, times(1)).buscarEnderecoPorCep(cepInvalido);
    }

    @Test
    @DisplayName("Deve retornar erro 404 para CEP não encontrado")
    void deveRetornarErro404ParaCepNaoEncontrado() throws Exception {
        // Given
        String cepNaoEncontrado = "99999999";
        when(cepService.buscarEnderecoPorCep(cepNaoEncontrado))
                .thenThrow(new RuntimeException("CEP não encontrado: " + cepNaoEncontrado));

        // When
        Exchange exchange = producerTemplate.request("direct:consultarCep", processor -> {
            processor.getIn().setHeader("cep", cepNaoEncontrado);
        });

        // Then
        assertNotNull(exchange);
        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        assertEquals(404, statusCode);
        String body = exchange.getMessage().getBody(String.class);
        assertTrue(body.contains("CEP não encontrado"));
        verify(cepService, times(1)).buscarEnderecoPorCep(cepNaoEncontrado);
    }

    @Test
    @DisplayName("Deve consultar endereço por UF, cidade e logradouro com sucesso")
    void deveConsultarEnderecoPorUfCidadeLogradouroComSucesso() throws Exception {
        // Given
        String uf = "SP";
        String cidade = "São Paulo";
        String logradouro = "Paulista";
        List<Endereco> enderecos = Arrays.asList(enderecoValido);
        when(cepService.buscarPorEndereco(uf, cidade, logradouro)).thenReturn(enderecos);

        // When
        Exchange exchange = producerTemplate.request("direct:consultarEndereco", processor -> {
            processor.getIn().setHeader("uf", uf);
            processor.getIn().setHeader("cidade", cidade);
            processor.getIn().setHeader("logradouro", logradouro);
        });

        // Then
        assertNotNull(exchange);
        assertNotNull(exchange.getMessage().getBody());
        assertEquals(enderecos, exchange.getMessage().getBody());
        verify(cepService, times(1)).buscarPorEndereco(uf, cidade, logradouro);
    }

    @Test
    @DisplayName("Deve retornar erro 400 para UF inválida")
    void deveRetornarErro400ParaUfInvalida() throws Exception {
        // Given
        String ufInvalida = "S";
        String cidade = "São Paulo";
        String logradouro = "Paulista";
        when(cepService.buscarPorEndereco(ufInvalida, cidade, logradouro))
                .thenThrow(new IllegalArgumentException("UF deve ter 2 caracteres"));

        // When
        Exchange exchange = producerTemplate.request("direct:consultarEndereco", processor -> {
            processor.getIn().setHeader("uf", ufInvalida);
            processor.getIn().setHeader("cidade", cidade);
            processor.getIn().setHeader("logradouro", logradouro);
        });

        // Then
        assertNotNull(exchange);
        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        assertEquals(400, statusCode);
        String body = exchange.getMessage().getBody(String.class);
        assertTrue(body.contains("UF deve ter 2 caracteres"));
        verify(cepService, times(1)).buscarPorEndereco(ufInvalida, cidade, logradouro);
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando nenhum endereço é encontrado")
    void deveRetornarErro404QuandoNenhumEnderecoEncontrado() throws Exception {
        // Given
        String uf = "SP";
        String cidade = "Cidade Inexistente";
        String logradouro = "Rua Inexistente";
        when(cepService.buscarPorEndereco(uf, cidade, logradouro))
                .thenThrow(new RuntimeException("Nenhum endereço encontrado"));

        // When
        Exchange exchange = producerTemplate.request("direct:consultarEndereco", processor -> {
            processor.getIn().setHeader("uf", uf);
            processor.getIn().setHeader("cidade", cidade);
            processor.getIn().setHeader("logradouro", logradouro);
        });

        // Then
        assertNotNull(exchange);
        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        assertEquals(404, statusCode);
        String body = exchange.getMessage().getBody(String.class);
        assertTrue(body.contains("Nenhum endereço encontrado"));
        verify(cepService, times(1)).buscarPorEndereco(uf, cidade, logradouro);
    }

    @Test
    @DisplayName("Deve executar health check com sucesso")
    void deveExecutarHealthCheckComSucesso() {
        // When
        String response = producerTemplate.requestBody("direct:healthCheck", null, String.class);

        // Then
        assertNotNull(response);
        assertTrue(response.contains("\"status\": \"UP\""));
        assertTrue(response.contains("\"service\": \"ViaCEP API\""));
    }

    @Test
    @DisplayName("Deve retornar erro 500 para exceção genérica")
    void deveRetornarErro500ParaExcecaoGenerica() throws Exception {
        // Given
        String cep = "01001000";
        when(cepService.buscarEnderecoPorCep(cep))
                .thenThrow(new RuntimeException("Erro de conexão"));

        // When
        Exchange exchange = producerTemplate.request("direct:consultarCep", processor -> {
            processor.getIn().setHeader("cep", cep);
        });

        // Then
        assertNotNull(exchange);
        Integer statusCode = exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        assertEquals(500, statusCode);
        String body = exchange.getMessage().getBody(String.class);
        assertTrue(body.contains("Erro interno do servidor"));
        verify(cepService, times(1)).buscarEnderecoPorCep(cep);
    }
}