package com.mrsalustiano.spring.camel.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Endereco - Testes do Modelo")
public class EnderecoTest {
    private ObjectMapper objectMapper;
    private Endereco endereco;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        endereco = new Endereco();
    }

    @Test
    @DisplayName("Deve criar endereço com construtor padrão")
    void deveCriarEnderecoComConstrutorPadrao() {
        // When
        Endereco novoEndereco = new Endereco();

        // Then
        assertNotNull(novoEndereco);
        assertNull(novoEndereco.getCep());
        assertNull(novoEndereco.getLogradouro());
        assertNull(novoEndereco.getBairro());
        assertNull(novoEndereco.getLocalidade());
        assertNull(novoEndereco.getUf());
        assertNull(novoEndereco.getErro());
    }

    @Test
    @DisplayName("Deve criar endereço com construtor completo")
    void deveCriarEnderecoComConstrutorCompleto() {
        // When
        Endereco novoEndereco = new Endereco(
                "01001-000",
                "Praça da Sé",
                "lado ímpar",
                "Sé",
                "São Paulo",
                "SP",
                "3550308",
                "1004",
                "11",
                "7107",
                null
        );

        // Then
        assertNotNull(novoEndereco);
        assertEquals("01001-000", novoEndereco.getCep());
        assertEquals("Praça da Sé", novoEndereco.getLogradouro());
        assertEquals("lado ímpar", novoEndereco.getComplemento());
        assertEquals("Sé", novoEndereco.getBairro());
        assertEquals("São Paulo", novoEndereco.getLocalidade());
        assertEquals("SP", novoEndereco.getUf());
        assertEquals("3550308", novoEndereco.getIbge());
        assertEquals("1004", novoEndereco.getGia());
        assertEquals("11", novoEndereco.getDdd());
        assertEquals("7107", novoEndereco.getSiafi());
    }

    @Test
    @DisplayName("Deve definir e obter todos os campos corretamente")
    void deveDefinirEObterTodosCamposCorretamente() {
        // When
        endereco.setCep("01001-000");
        endereco.setLogradouro("Praça da Sé");
        endereco.setComplemento("lado ímpar");
        endereco.setBairro("Sé");
        endereco.setLocalidade("São Paulo");
        endereco.setUf("SP");
        endereco.setIbge("3550308");
        endereco.setGia("1004");
        endereco.setDdd("11");
        endereco.setSiafi("7107");
        endereco.setErro(false);

        // Then
        assertEquals("01001-000", endereco.getCep());
        assertEquals("Praça da Sé", endereco.getLogradouro());
        assertEquals("lado ímpar", endereco.getComplemento());
        assertEquals("Sé", endereco.getBairro());
        assertEquals("São Paulo", endereco.getLocalidade());
        assertEquals("SP", endereco.getUf());
        assertEquals("3550308", endereco.getIbge());
        assertEquals("1004", endereco.getGia());
        assertEquals("11", endereco.getDdd());
        assertEquals("7107", endereco.getSiafi());
        assertEquals(false, endereco.getErro());
    }

    @Test
    @DisplayName("Deve serializar para JSON corretamente")
    void deveSerializarParaJsonCorretamente() throws Exception {
        // Given
        endereco.setCep("01001-000");
        endereco.setLogradouro("Praça da Sé");
        endereco.setBairro("Sé");
        endereco.setLocalidade("São Paulo");
        endereco.setUf("SP");
        endereco.setErro(false);

        // When
        String json = objectMapper.writeValueAsString(endereco);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"cep\":\"01001-000\""));
        assertTrue(json.contains("\"logradouro\":\"Praça da Sé\""));
        assertTrue(json.contains("\"bairro\":\"Sé\""));
        assertTrue(json.contains("\"localidade\":\"São Paulo\""));
        assertTrue(json.contains("\"uf\":\"SP\""));
        assertTrue(json.contains("\"erro\":false"));
    }

    @Test
    @DisplayName("Deve deserializar de JSON corretamente")
    void deveDeserializarDeJsonCorretamente() throws Exception {
        // Given
        String json = """
            {
                "cep": "01001-000",
                "logradouro": "Praça da Sé",
                "complemento": "lado ímpar",
                "bairro": "Sé",
                "localidade": "São Paulo",
                "uf": "SP",
                "ibge": "3550308",
                "gia": "1004",
                "ddd": "11",
                "siafi": "7107",
                "erro": false
            }
            """;

        // When
        Endereco enderecoDeserializado = objectMapper.readValue(json, Endereco.class);

        // Then
        assertNotNull(enderecoDeserializado);
        assertEquals("01001-000", enderecoDeserializado.getCep());
        assertEquals("Praça da Sé", enderecoDeserializado.getLogradouro());
        assertEquals("lado ímpar", enderecoDeserializado.getComplemento());
        assertEquals("Sé", enderecoDeserializado.getBairro());
        assertEquals("São Paulo", enderecoDeserializado.getLocalidade());
        assertEquals("SP", enderecoDeserializado.getUf());
        assertEquals("3550308", enderecoDeserializado.getIbge());
        assertEquals("1004", enderecoDeserializado.getGia());
        assertEquals("11", enderecoDeserializado.getDdd());
        assertEquals("7107", enderecoDeserializado.getSiafi());
        assertEquals(false, enderecoDeserializado.getErro());
    }

    @Test
    @DisplayName("Deve deserializar JSON com erro=true")
    void deveDeserializarJsonComErroTrue() throws Exception {
        // Given
        String json = """
            {
                "erro": true
            }
            """;

        // When
        Endereco enderecoComErro = objectMapper.readValue(json, Endereco.class);

        // Then
        assertNotNull(enderecoComErro);
        assertTrue(enderecoComErro.getErro());
        assertNull(enderecoComErro.getCep());
        assertNull(enderecoComErro.getLogradouro());
    }



    @Test
    @DisplayName("Deve lidar com campos nulos")
    void deveLidarComCamposNulos() {
        // Given & When
        endereco.setCep(null);
        endereco.setLogradouro(null);
        endereco.setBairro(null);
        endereco.setLocalidade(null);
        endereco.setUf(null);
        endereco.setErro(null);

        // Then
        assertNull(endereco.getCep());
        assertNull(endereco.getLogradouro());
        assertNull(endereco.getBairro());
        assertNull(endereco.getLocalidade());
        assertNull(endereco.getUf());
        assertNull(endereco.getErro());

        // ToString não deve quebrar com campos nulos
        assertDoesNotThrow(() -> endereco.toString());
    }
}
