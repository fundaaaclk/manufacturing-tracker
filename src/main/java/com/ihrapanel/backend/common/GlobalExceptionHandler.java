package com.ihrapanel.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;


// @RestControllerAdvice: tum controller'larda fırlatilan exception'lari
// burada tek bir yerden yakalar. Bu olmadan, ornegin CompanyService'teki
// IllegalArgumentException direkt kullaniciya cirkin bir 500 Internal Server
// Error + stack trace olarak doner - guvenlik acisindan da kotu (ic yapimizi
// disari sizdirir), kullanici acisindan da anlamsiz.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Servis katmaninda "throw new IllegalArgumentException(...)" yazdigimiz
    // her yer (ornegin CompanyService.createCompany) buraya duser ve
    // temiz bir 400 Bad Request + anlasilir mesaj olarak kullaniciya doner.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    // Beklemedigimiz her turlu hata (null pointer, database hatasi vb.) icin
    // son cikis kapisi. Kullaniciya asla ic detay/stack trace gostermeyiz -
    // sadece genel bir mesaj + 500. Gercek hata loglara duser (Spring
    // bunu otomatik konsola yazar), kullaniciya sizdirilmaz.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Beklenmeyen bir hata olustu."));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, String>> handleValidationException(
        MethodArgumentNotValidException ex
) {

    Map<String, String> errors = new HashMap<>();

    ex.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                    errors.put(
                            error.getField(),
                            error.getDefaultMessage()
                    )
            );

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors);
}
}