package org.example.noteservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class TagDTO {
    @NotBlank
    @Size(max = 50, message = "Максимальная длинна тега 50 символов")
    String name;
}
