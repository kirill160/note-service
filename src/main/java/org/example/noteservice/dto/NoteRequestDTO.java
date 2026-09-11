package org.example.noteservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;

@Value
public class NoteRequestDTO {
    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(max = 250, message = "Неверное количество символов в заголовке, минимально 1 максимально 250")
    String title;
    @NotBlank(message = "Текст не может быть пустым")
    @Size(max = 1000, message = "Минимальное количество символов в заметке 1 максимальное 1000")
    String textOfNote;
    @Valid
    @NotEmpty
    @Size(max = 10, message = "Максимум тегов")
    List<TagDTO> tags;
    boolean archive;
}