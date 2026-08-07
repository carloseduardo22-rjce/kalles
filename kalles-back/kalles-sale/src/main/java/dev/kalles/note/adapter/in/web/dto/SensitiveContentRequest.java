package dev.kalles.note.adapter.in.web.dto;

import lombok.Data;

@Data
public class SensitiveContentRequest {
    private String plainText;
    private String secret;
}
