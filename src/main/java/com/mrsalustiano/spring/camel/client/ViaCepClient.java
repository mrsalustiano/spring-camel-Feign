package com.mrsalustiano.spring.camel.client;

import com.mrsalustiano.spring.camel.model.Endereco;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "viaCepClient", url = "${viacep.api.url}")
public interface ViaCepClient {

    @GetMapping("/{cep}/json/")
    Endereco consultarCep(@PathVariable("cep") String cep);

    // 👇 Novo método para consulta por endereço
    @GetMapping("/{uf}/{cidade}/{logradouro}/json/")
    List<Endereco> consultarPorEndereco(
            @PathVariable("uf") String uf,
            @PathVariable("cidade") String cidade,
            @PathVariable("logradouro") String logradouro);
}
