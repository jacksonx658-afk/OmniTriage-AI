package com.example.Dev.OmniTriage.AI.domain;

public record AtendimentoIaResponse(
        Categoria categoria,
        NivelUrgencia nivelUrgencia,
        String resposta
) {
}
