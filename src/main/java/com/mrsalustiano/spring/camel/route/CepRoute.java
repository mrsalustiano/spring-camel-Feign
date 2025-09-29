package com.mrsalustiano.spring.camel.route;

import com.mrsalustiano.spring.camel.service.CepService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.camel.model.rest.RestBindingMode;
import com.mrsalustiano.spring.camel.model.Endereco;


@Component
public class CepRoute extends RouteBuilder {

    @Autowired
    private CepService cepService;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("Erro genérico capturado: ${exception.message}")
                .setHeader("CamelHttpResponseCode", constant(500))
                .setBody(constant("{\"erro\": \"Erro interno do servidor\"}"));

        // Configuração do REST DSL
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .port(8080)
                .contextPath("/api")
                .apiProperty("api.title", "ViaCEP API")
                .apiProperty("api.version", "1.0.0")
                .apiProperty("api.description", "API para consulta de CEP usando ViaCEP");

        // Endpoint REST para consulta de CEP
        rest("/cep")
                .description("Serviço de consulta de CEP")
                .get("/{cep}")
                .description("Consulta endereço por CEP")
                .param().name("cep").type(RestParamType.path).description("CEP a ser consultado").dataType("string").endParam()
                .responseMessage().code(200).message("CEP encontrado").endResponseMessage()
                .responseMessage().code(400).message("CEP inválido").endResponseMessage()
                .responseMessage().code(404).message("CEP não encontrado").endResponseMessage()
                .responseMessage().code(500).message("Erro interno do servidor").endResponseMessage()
                .to("direct:consultarCep");

        // Rota para processar a consulta de CEP
        from("direct:consultarCep")
                .routeId("consultarCepRoute")
                .log("Consultando CEP: ${header.cep}")
                .doTry()
                .process(exchange -> {
                    String cep = exchange.getIn().getHeader("cep", String.class);
                    Endereco endereco = cepService.buscarEnderecoPorCep(cep);
                    exchange.getIn().setBody(endereco);
                })
                .log("CEP consultado com sucesso: ${body}")
                .doCatch(IllegalArgumentException.class)
                .log("CEP inválido: ${exception.message}")
                .setHeader("CamelHttpResponseCode", constant(400))
                .setBody(simple("{\"erro\": \"${exception.message}\"}"))
                .doCatch(RuntimeException.class)
                .log("Erro ao consultar CEP: ${exception.message}")
                .choice()
                .when(simple("${exception.message} contains 'não encontrado'"))
                .setHeader("CamelHttpResponseCode", constant(404))
                .setBody(simple("{\"erro\": \"${exception.message}\"}"))
                .otherwise()
                .setHeader("CamelHttpResponseCode", constant(500))
                .setBody(simple("{\"erro\": \"Erro interno do servidor\"}"))
                .end();

        rest("/endereco")
                .description("Serviço de consulta de endereço")
                .get("/{uf}/{cidade}/{logradouro}")
                .description("Pesquisa endereços por UF, cidade e logradouro")
                .param().name("uf").type(RestParamType.path).description("UF do estado (2 letras)").dataType("string").endParam()
                .param().name("cidade").type(RestParamType.path).description("Nome da cidade").dataType("string").endParam()
                .param().name("logradouro").type(RestParamType.path).description("Nome do logradouro").dataType("string").endParam()
                .responseMessage().code(200).message("Endereços encontrados").endResponseMessage()
                .responseMessage().code(400).message("Parâmetros inválidos").endResponseMessage()
                .responseMessage().code(404).message("Nenhum endereço encontrado").endResponseMessage()
                .responseMessage().code(500).message("Erro interno do servidor").endResponseMessage()
                .to("direct:consultarEndereco");

        from("direct:consultarEndereco")
                .routeId("consultarEnderecoRoute")
                .log("Consultando endereço: ${header.uf}/${header.cidade}/${header.logradouro}")
                .doTry()
                .process(exchange -> {
                    String uf = exchange.getIn().getHeader("uf", String.class);
                    String cidade = exchange.getIn().getHeader("cidade", String.class);
                    String logradouro = exchange.getIn().getHeader("logradouro", String.class);

                    exchange.getIn().setBody(
                            cepService.buscarPorEndereco(uf, cidade, logradouro)
                    );
                })
                .doCatch(IllegalArgumentException.class)
                .setHeader("CamelHttpResponseCode", constant(400))
                .setBody(simple("{\"erro\":\"${exception.message}\"}"))
                .doCatch(RuntimeException.class)
                .setHeader("CamelHttpResponseCode", constant(404))
                .setBody(simple("{\"erro\":\"${exception.message}\"}"))
                .end();

        // Rota de health check
        rest("/health")
                .description("Health check")
                .get()
                .description("Verifica se a aplicação está funcionando")
                .responseMessage().code(200).message("Aplicação funcionando").endResponseMessage()
                .to("direct:healthCheck");

        from("direct:healthCheck")
                .routeId("healthCheckRoute")
                .setBody(constant("{\"status\": \"UP\", \"service\": \"ViaCEP API\"}"))
                .setHeader("Content-Type", constant("application/json"));
    }



}
