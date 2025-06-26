package com.walletservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Estrutura básica para resposta de erro
    private Map<String, Object> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("message", message);
        errorDetails.put("status", status.value());

        // Adicionando um campo extra para ajudar no debug
        if (!status.is2xxSuccessful()) {
            errorDetails.put("error", status.getReasonPhrase());
        }

        return errorDetails;
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Object> handleWalletNotFoundException(WalletNotFoundException ex, WebRequest request) {
        // Carteira não encontrada - provavelmente ID de usuário incorreto
        return new ResponseEntity<>(
            createErrorResponse("Carteira não encontrada: " + ex.getMessage(), HttpStatus.NOT_FOUND),
            HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Object> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        // Saldo insuficiente - usuário tentou sacar ou transferir mais do que tem
        return new ResponseEntity<>(
            createErrorResponse("Saldo insuficiente para completar a operação", HttpStatus.BAD_REQUEST),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<Object> handleWalletAlreadyExistsException(WalletAlreadyExistsException ex, WebRequest request) {
        // Tentativa de criar carteira duplicada
        return new ResponseEntity<>(
            createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT),
            HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Parâmetros inválidos - como valor negativo para depósito
        return new ResponseEntity<>(
            createErrorResponse("Parâmetro inválido: " + ex.getMessage(), HttpStatus.BAD_REQUEST),
            HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        // Erros de validação dos campos da requisição
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Erro de validação");
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Captura qualquer exceção não tratada
    // TODO: Em produção, não expor detalhes técnicos do erro
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        // Em ambiente de produção, remover o stacktrace e logar o erro
        return new ResponseEntity<>(
            createErrorResponse("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
                               HttpStatus.INTERNAL_SERVER_ERROR),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
