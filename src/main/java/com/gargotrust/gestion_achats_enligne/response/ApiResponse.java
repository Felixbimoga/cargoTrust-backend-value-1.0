package com.gargotrust.gestion_achats_enligne.response;

import java.util.List;

public class ApiResponse<T> {

    private List<String> errors;
    private Payload<T> payload;

    public ApiResponse(List<String> errors, Payload<T> payload) {
        this.errors = errors;
        this.payload = payload;
    }

    public List<String> getErrors() {
        return errors;
    }

    public Payload<T> getPayload() {
        return payload;
    }

    public static class Payload<T> {
        private String message;
        private T data;

        public Payload(String message, T data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }
    }
}

