package com.ihrapanel.backend.common;

// API'den donen hata mesajlarinin standart sekli.
// Boylece her hata { "message": "..." } seklinde tutarli bir JSON olarak doner.
public class ErrorResponse {

    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}