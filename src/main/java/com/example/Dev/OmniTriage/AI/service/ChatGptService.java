package com.example.Dev.OmniTriage.AI.service;
import com.example.Dev.OmniTriage.AI.domain.AtendimentoIaResponse;
import com.example.Dev.OmniTriage.AI.domain.Categoria;
import com.example.Dev.OmniTriage.AI.domain.NivelUrgencia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class ChatGptService {

    private final DocumentacaoService documentacaoService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apikey;

    @Value("${openai.model}")
    private String model;


    public ChatGptService(DocumentacaoService documentacaoService, WebClient webClient, ObjectMapper objectMapper) {
        this.documentacaoService = documentacaoService;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    public Mono<AtendimentoIaResponse> generateAtendiment(String mensagemCliente) {
        String prompt = """
        Você é um assistente de atendimento ao cliente.

        REGRAS:
        - Responda SOMENTE com base na documentação abaixo.
        - Se a documentação não cobrir o assunto, use categoria OUTROS, urgência ALTA
          e a resposta: "Vou encaminhar sua solicitação para um atendente humano."
        - Nunca invente prazos, valores ou políticas.
        - Seja cordial e objetivo.

        FORMATO DA SAÍDA:
        Responda APENAS com um JSON válido, sem texto antes ou depois e sem markdown:
        {"categoria": "...", "nivelUrgencia": "...", "resposta": "..."}

        Valores permitidos:
        - categoria: TROCA, REEMBOLSO, INFORMAR_DEFEITO, DUVIDA_REGRAS, OUTROS
        - nivelUrgencia: BAIXA, MEDIA, ALTA

        DOCUMENTAÇÃO DA EMPRESA:
        %s

        MENSAGEM DO CLIENTE:
        %s
        """.formatted(documentacaoService.getDocumentacao(), mensagemCliente);


        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", prompt
        );
        System.out.println("MODELO ENVIADO: [" + model + "]");
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
                    return "";
                })
                .map(this::converterJson)
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.out.println("ERRO DA IA: " + e.getResponseBodyAsString());
                    return Mono.just(respostaPadrao());
                });
    }
    private AtendimentoIaResponse converterJson(String texto) {
        try {
            String limpo = texto.replace("```json", "").replace("```", "").trim();
            AtendimentoIaResponse ia = objectMapper.readValue(limpo, AtendimentoIaResponse.class);

            if (ia.categoria() == null || ia.nivelUrgencia() == null
                    || ia.resposta() == null || ia.resposta().isBlank()) {
                return respostaPadrao();
            }
            return ia;
        } catch (Exception e) {
            System.out.println("JSON inválido da IA: " + texto);
            return respostaPadrao();
        }
    }
    private AtendimentoIaResponse respostaPadrao() {
        return new AtendimentoIaResponse(
                Categoria.OUTROS,
                NivelUrgencia.ALTA,
                "Vou encaminhar sua solicitação para um atendente humano."
        );
    }
}



