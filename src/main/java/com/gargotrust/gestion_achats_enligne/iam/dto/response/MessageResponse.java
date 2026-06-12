package com.gargotrust.gestion_achats_enligne.iam.dto.response;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MessageResponse {
    private String message;
    private boolean success;
}
