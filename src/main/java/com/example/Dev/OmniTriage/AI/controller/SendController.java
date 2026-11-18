package com.example.Dev.OmniTriage.AI.controller;
import com.example.Dev.OmniTriage.AI.domain.Atendimento;
import com.example.Dev.OmniTriage.AI.service.AtendimentoService;
import com.example.Dev.OmniTriage.AI.service.ChatGptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class SendController {

    private static final int TAMANHO_MAXIMO = 1000;
    private final AtendimentoService service;
    private final ChatGptService chatGptService;
    public SendController(AtendimentoService service, ChatGptService chatGptService) {
        this.service = service;
        this.chatGptService = chatGptService;
    }

    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> gerar(@RequestBody String mensagem) {

        if (mensagem == null || mensagem.isBlank()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body("A mensagem não pode estar vazia."));
        }

        if (mensagem.length() > TAMANHO_MAXIMO) {
            return Mono.just(ResponseEntity.badRequest()
                    .body("A mensagem deve ter no máximo " + TAMANHO_MAXIMO + " caracteres."));
        }

        return chatGptService.generateAtendiment(mensagem)
                .flatMap(ia -> Mono.fromCallable(() -> {
                    Atendimento atendimento = new Atendimento();
                    atendimento.setMensagem(mensagem);
                    atendimento.setCategoria(ia.categoria());
                    atendimento.setNivelUrgencia(ia.nivelUrgencia());
                    atendimento.setResposta(ia.resposta());
                    atendimento.setDataCriacao(LocalDateTime.now());
                    service.registrarAtendimento(atendimento);
                    return ia.resposta();
                }).subscribeOn(Schedulers.boundedElastic()))
                .map(ResponseEntity::ok);
    }
}