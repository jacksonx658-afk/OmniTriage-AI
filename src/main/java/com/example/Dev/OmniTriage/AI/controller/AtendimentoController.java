package com.example.Dev.OmniTriage.AI.controller;

import com.example.Dev.OmniTriage.AI.domain.Atendimento;
import com.example.Dev.OmniTriage.AI.service.AtendimentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/atendimento")

public class AtendimentoController {

    private final AtendimentoService service;
    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    //GET
    @GetMapping("/listar")
    public ResponseEntity <List<Atendimento>> listar(){
        List<Atendimento> lista = service.listarAtendimento();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("listar/{id}")
    public ResponseEntity<?> listarPorId(@PathVariable Long id) {
        Optional<Atendimento> atendimento = service.listarAtendimentoPorId(id);
        if (atendimento.isPresent()) {
            return ResponseEntity.ok(atendimento.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Id inexistente!");
        }
    }


    @PostMapping("/criar")
    //POST
    public ResponseEntity<?> registrar(@RequestBody Atendimento atendimento ){
        service.registrarAtendimento(atendimento);
        return ResponseEntity.status(HttpStatus.CREATED).body("Atedimento salvo com sucesso!");
    }



    // UPDATE
    @PutMapping("alterar/{id}")
    public ResponseEntity<Atendimento> atualizar(@RequestBody Atendimento novo, @PathVariable Long id) {
        return service.listarAtendimentoPorId(id)
                .map(itemExistente -> {
                    novo.setId(itemExistente.getId());
                    Atendimento atualizado = service.atualizarAtendimento(novo);
                    return ResponseEntity.ok(atualizado);
                })
                .orElse(ResponseEntity.notFound().build());
        }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        if (service.listarAtendimentoPorId(id).isPresent()) {
            service.deletarAtendimento(id);
            return ResponseEntity.ok("Deletado com sucesso!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Id inexistente!!");
    }

}
