package com.mrsalustiano.spring.camel.service;

import com.mrsalustiano.spring.camel.client.ViaCepClient;
import com.mrsalustiano.spring.camel.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("CepService - Testes Unitários")
public class CepServiceTest {

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private CepService cepService;

    private Endereco enderecoValido;
    private Endereco enderecoComErro;

    @BeforeEach
    void setUp() {
        enderecoValido = new Endereco();
        enderecoValido.setCep("01001-000");
        enderecoValido.setLogradouro("Praça da Sé");
        enderecoValido.setBairro("Sé");
        enderecoValido.setLocalidade("São Paulo");
        enderecoValido.setUf("SP");
        enderecoValido.setErro(false);

        enderecoComErro = new Endereco();
        enderecoComErro.setErro(true);
    }

    @Test
    @DisplayName("Deve buscar endereço por CEP válido com sucesso")
    void deveBuscarEnderecoPorCepValidoComSucesso() {
        // Given
        String cep = "01001000";
        when(viaCepClient.consultarCep(cep)).thenReturn(enderecoValido);

        // When
        Endereco resultado = cepService.buscarEnderecoPorCep(cep);

        // Then
        assertNotNull(resultado);
        assertEquals("01001-000", resultado.getCep());
        assertEquals("Praça da Sé", resultado.getLogradouro());
        assertEquals("São Paulo", resultado.getLocalidade());
        assertEquals("SP", resultado.getUf());
        verify(viaCepClient, times(1)).consultarCep(cep);
    }

    @Test
    @DisplayName("Deve buscar endereço por CEP formatado com sucesso")
    void deveBuscarEnderecoPorCepFormatadoComSucesso() {
        // Given
        String cepFormatado = "01001-000";
        String cepLimpo = "01001000";
        when(viaCepClient.consultarCep(cepLimpo)).thenReturn(enderecoValido);

        // When
        Endereco resultado = cepService.buscarEnderecoPorCep(cepFormatado);

        // Then
        assertNotNull(resultado);
        verify(viaCepClient, times(1)).consultarCep(cepLimpo);
    }

    @Test
    @DisplayName("Deve lançar exceção para CEP com menos de 8 dígitos")
    void deveLancarExcecaoParaCepComMenosDe8Digitos() {
        // Given
        String cepInvalido = "123";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarEnderecoPorCep(cepInvalido)
        );

        assertEquals("CEP deve conter exatamente 8 dígitos", exception.getMessage());
        verify(viaCepClient, never()).consultarCep(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para CEP com mais de 8 dígitos")
    void deveLancarExcecaoParaCepComMaisDe8Digitos() {
        // Given
        String cepInvalido = "123456789";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarEnderecoPorCep(cepInvalido)
        );

        assertEquals("CEP deve conter exatamente 8 dígitos", exception.getMessage());
        verify(viaCepClient, never()).consultarCep(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando CEP não é encontrado")
    void deveLancarExcecaoQuandoCepNaoEncontrado() {
        // Given
        String cep = "99999999";
        when(viaCepClient.consultarCep(cep)).thenReturn(enderecoComErro);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cepService.buscarEnderecoPorCep(cep)
        );

        assertTrue(exception.getMessage().contains("CEP não encontrado"));
        verify(viaCepClient, times(1)).consultarCep(cep);
    }

    @Test
    @DisplayName("Deve buscar endereços por UF, cidade e logradouro com sucesso")
    void deveBuscarEnderecosPorUfCidadeLogradouroComSucesso() {
        // Given
        String uf = "SP";
        String cidade = "São Paulo";
        String logradouro = "Paulista";
        List<Endereco> enderecos = Arrays.asList(enderecoValido);
        when(viaCepClient.consultarPorEndereco(uf, cidade, logradouro)).thenReturn(enderecos);

        // When
        List<Endereco> resultado = cepService.buscarPorEndereco(uf, cidade, logradouro);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(viaCepClient, times(1)).consultarPorEndereco(uf, cidade, logradouro);
    }

    @Test
    @DisplayName("Deve lançar exceção para UF inválida")
    void deveLancarExcecaoParaUfInvalida() {
        // Given
        String ufInvalida = "S";
        String cidade = "São Paulo";
        String logradouro = "Paulista";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarPorEndereco(ufInvalida, cidade, logradouro)
        );

        assertEquals("UF deve ter 2 caracteres", exception.getMessage());
        verify(viaCepClient, never()).consultarPorEndereco(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para cidade nula")
    void deveLancarExcecaoParaCidadeNula() {
        // Given
        String uf = "SP";
        String cidade = null;
        String logradouro = "Paulista";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarPorEndereco(uf, cidade, logradouro)
        );

        assertEquals("Cidade e logradouro são obrigatórios", exception.getMessage());
        verify(viaCepClient, never()).consultarPorEndereco(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para cidade vazia")
    void deveLancarExcecaoParaCidadeVazia() {
        // Given
        String uf = "SP";
        String cidade = "";
        String logradouro = "Paulista";

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarPorEndereco(uf, cidade, logradouro)
        );

        assertEquals("Cidade e logradouro são obrigatórios", exception.getMessage());
        verify(viaCepClient, never()).consultarPorEndereco(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção para logradouro nulo")
    void deveLancarExcecaoParaLogradouroNulo() {
        // Given
        String uf = "SP";
        String cidade = "São Paulo";
        String logradouro = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.buscarPorEndereco(uf, cidade, logradouro)
        );

        assertEquals("Cidade e logradouro são obrigatórios", exception.getMessage());
        verify(viaCepClient, never()).consultarPorEndereco(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando nenhum endereço é encontrado")
    void deveLancarExcecaoQuandoNenhumEnderecoEncontrado() {
        // Given
        String uf = "SP";
        String cidade = "Cidade Inexistente";
        String logradouro = "Rua Inexistente";
        when(viaCepClient.consultarPorEndereco(uf, cidade, logradouro)).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cepService.buscarPorEndereco(uf, cidade, logradouro)
        );

        assertTrue(exception.getMessage().contains("Nenhum endereço encontrado"));
        verify(viaCepClient, times(1)).consultarPorEndereco(uf, cidade, logradouro);
    }

    @Test
    @DisplayName("Deve lançar exceção quando retorno é nulo")
    void deveLancarExcecaoQuandoRetornoNulo() {
        // Given
        String uf = "SP";
        String cidade = "São Paulo";
        String logradouro = "Paulista";
        when(viaCepClient.consultarPorEndereco(uf, cidade, logradouro)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> cepService.buscarPorEndereco(uf, cidade, logradouro)
        );

        assertTrue(exception.getMessage().contains("Nenhum endereço encontrado"));
        verify(viaCepClient, times(1)).consultarPorEndereco(uf, cidade, logradouro);
    }
}
