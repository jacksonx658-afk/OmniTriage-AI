package com.example.Dev.OmniTriage.AI.service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class DocumentacaoService {
    private final String documentacao;

    public DocumentacaoService() throws IOException {
        var resource = new ClassPathResource("docs/documentacao.txt");
        this.documentacao = resource.getContentAsString(StandardCharsets.UTF_8);
    }
    public String getDocumentacao() {
        return documentacao;
    }
}

