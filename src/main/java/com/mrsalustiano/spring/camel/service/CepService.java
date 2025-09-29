package com.mrsalustiano.spring.camel.service;

import com.mrsalustiano.spring.camel.client.ViaCepClient;
import com.mrsalustiano.spring.camel.model.Endereco;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CepService {

    private final ViaCepClient viaCepClient;

    public CepService(ViaCepClient viaCepClient) {
        this.viaCepClient = viaCepClient;
    }

    public Endereco buscarEnderecoPorCep(String cep) {
        String cepLimpo = normalizarCep(cep);
        validarCep(cepLimpo);

        Endereco dto = viaCepClient.consultarCep(cepLimpo);

        // ViaCEP retorna {"erro": true} quando não encontra
        if (dto == null || Boolean.TRUE.equals(dto.getErro())) {
            throw new RuntimeException("CEP não encontrado: " + cepLimpo);
        }
        return dto;
    }

    public List<Endereco> buscarPorEndereco(String uf, String cidade, String logradouro) {
        validarUf(uf);
        validarCidadeLogradouro(cidade, logradouro);

        List<Endereco> lista = viaCepClient.consultarPorEndereco(uf.toUpperCase(), cidade, logradouro);

        if (lista == null || lista.isEmpty()) {
            throw new RuntimeException("Nenhum endereço encontrado");
        }
        return lista;
    }

    private String normalizarCep(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }

    private void validarCep(String cepLimpo) {
        if (cepLimpo.length() != 8) {
            throw new IllegalArgumentException("CEP deve conter exatamente 8 dígitos");
        }
    }

    private void validarUf(String uf) {
        if (uf == null || uf.trim().length() != 2) {
            throw new IllegalArgumentException("UF deve ter 2 caracteres");
        }
    }

    private void validarCidadeLogradouro(String cidade, String logradouro) {
        if (cidade == null || cidade.isBlank() || logradouro == null || logradouro.isBlank()) {
            throw new IllegalArgumentException("Cidade e logradouro são obrigatórios");
        }
    }
}
