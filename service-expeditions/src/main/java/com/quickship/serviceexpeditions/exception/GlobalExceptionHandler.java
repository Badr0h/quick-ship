package com.quickship.serviceexpeditions.exception;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> refus(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<String> notFound(RessourceIntrouvableException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
    @ExceptionHandler(ServiceExterneIndisponibleException.class)
    public ResponseEntity<String> serviceExterne(ServiceExterneIndisponibleException ex) {
        return ResponseEntity.status(503).body(ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> validation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body("Données invalides");
    }
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> feignNotFound(FeignException.NotFound ex) {
        return ResponseEntity.status(404).body("Ressource externe introuvable");
    }
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> feign(FeignException ex) {
        if (ex.status() == -1 || ex.status() == 503) {
            return ResponseEntity.status(503).body("Échec de communication avec un service");
        }
        return ResponseEntity.status(502).body("Erreur HTTP du service distant : " + ex.status());
    }
}
