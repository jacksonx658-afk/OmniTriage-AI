package com.example.Dev.OmniTriage.AI.service;
import com.example.Dev.OmniTriage.AI.domain.Atendimento;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ChatGptService {

    private final DocumentacaoService documentacaoService;
    private final WebClient webClient;
    private String apikey = System.getenv("API_KEY");

    public ChatGptService(DocumentacaoService documentacaoService, WebClient webClient) {
        this.documentacaoService = documentacaoService;
        this.webClient = webClient;
    }

    public Mono<String> generateAtendiment(String mensagemCliente) {
        String prompt = """
                Você é um assistente de atendimento ao cliente.

                REGRAS:
                - Responda SOMENTE com base na documentação abaixo.
                - Se a documentação não cobrir o assunto, diga exatamente:
                  "Vou encaminhar sua solicitação para um atendente humano."
                - Nunca invente prazos, valores ou políticas.
                - Seja cordial e objetivo.

                DOCUMENTAÇÃO DA EMPRESA:
                %s

                MENSAGEM DO CLIENTE:
                %s
                """.formatted(documentacaoService.getDocumentacao(), mensagemCliente);


        Map<String, Object> requestBody = Map.of(
                "model", "gpt-6-luna",
                "input", prompt
        );

        return webClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apikey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    var output = (List<Map<String, Object>>) response.get("output");

                    if (output != null) {
                        for (Map<String, Object> step : output) {
                            if ("message".equals(step.get("type"))) {
                                var contentList = (List<Map<String, Object>>) step.get("content");
                                if (contentList != null && !contentList.isEmpty()) {
                                    return contentList.get(0).get("text").toString();
                                }
                            }
                        }
                    }

                    return "Nenhuma resposta foi gerada.";
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.out.println("ERRO DA IA: " + e.getResponseBodyAsString());
                    return Mono.just("Não foi possível gerar a resposta.");
                });
    }
}



