package com.example.Dev.OmniTriage.AI.controller;
import com.example.Dev.OmniTriage.AI.controller.UIController;
import com.example.Dev.OmniTriage.AI.domain.Atendimento;
import com.example.Dev.OmniTriage.AI.domain.AtendimentoIaResponse;
import com.example.Dev.OmniTriage.AI.service.AtendimentoService;
import com.example.Dev.OmniTriage.AI.service.ChatGptService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Controller
public class UIController {

    private static final int TAMANHO_MAXIMO = 1000;

    private final AtendimentoService service;
    private final ChatGptService chatGptService;

    public UIController(AtendimentoService service, ChatGptService chatGptService) {
        this.service = service;
        this.chatGptService = chatGptService;
    }

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @PostMapping("/enviar")
    public String enviar(@RequestParam(defaultValue = "") String mensagem, Model model) {
        model.addAttribute("mensagem", mensagem);

        if (mensagem.isBlank()) {
            model.addAttribute("erro", "Escreva sua mensagem antes de enviar.");
            return "index";
        }

        if (mensagem.length() > TAMANHO_MAXIMO) {
            model.addAttribute("erro", "A mensagem deve ter no máximo " + TAMANHO_MAXIMO + " caracteres.");
            return "index";
        }

        AtendimentoIaResponse ia = chatGptService.generateAtendiment(mensagem).block();

        if (ia == null) {
            model.addAttribute("erro", "Não foi possível gerar a resposta. Tente novamente.");
            return "index";
        }

        Atendimento atendimento = new Atendimento();
        atendimento.setMensagem(mensagem);
        atendimento.setCategoria(ia.categoria());
        atendimento.setNivelUrgencia(ia.nivelUrgencia());
        atendimento.setResposta(ia.resposta());
        atendimento.setDataCriacao(LocalDateTime.now());
        service.registrarAtendimento(atendimento);

        model.addAttribute("resposta", ia.resposta());
        return "index";
    }

    @GetMapping("/painel")
    public String painel(Model model) {
        List<Atendimento> lista = service.listarAtendimento().stream()
                .sorted(Comparator.comparing(Atendimento::getDataCriacao,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("atendimentos", lista);
        return "painel";
    }
}
