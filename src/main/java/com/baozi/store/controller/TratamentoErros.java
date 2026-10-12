package com.baozi.store.controller;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

// Centraliza respostas JSON simples para os erros previstos da API.
@RestControllerAdvice
public class TratamentoErros {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> tratarRegistro(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(Map.of("mensagem", erro.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> tratarValidacao(MethodArgumentNotValidException erro) {
        String mensagem = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(" "));
        return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> tratarFormato(Exception erro) {
        return ResponseEntity.badRequest().body(Map.of("mensagem",
                "Formato inválido. Confira o JSON, os tipos dos campos, a data (AAAA-MM-DD) e o ID da URL."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> tratarRelacionamento(DataIntegrityViolationException erro) {
        // A chave estrangeira também protege os vínculos em requisições simultâneas.
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensagem",
                "A operação conflita com os registros vinculados. Consulte os clientes, produtos e pedidos."));
    }
}
