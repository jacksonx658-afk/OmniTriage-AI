package com.example.Dev.OmniTriage.AI.service;

import com.example.Dev.OmniTriage.AI.domain.Atendimento;
import com.example.Dev.OmniTriage.AI.repository.AtendimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
public class AtendimentoService {

    private final AtendimentoRepository repository;

    public AtendimentoService(AtendimentoRepository repository) {
        this.repository = repository;
    }


    // Listar
    public List<Atendimento> listarAtendimento(){
        return repository.findAll();
    }

    // ListarPorId
    public Optional<Atendimento> listarAtendimentoPorId(Long id) {
        return repository.findById(id);

    }


    // Criar
    public Atendimento registrarAtendimento(Atendimento atendimento){
        return repository.save(atendimento);
    }

    // Deletar

    public void deletarAtendimento(Long id){
        repository.deleteById(id);
    }

    // Upadate

    public Atendimento atualizarAtendimento(Atendimento novo){
        return repository.save(novo);
    }

}
