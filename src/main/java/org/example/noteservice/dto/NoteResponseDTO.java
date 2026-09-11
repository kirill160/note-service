package org.example.noteservice.dto;

import lombok.Value;

import java.util.List;

@Value
public class NoteResponseDTO {
    Long id;
    String title;
    String text;
    boolean archived;
    List<TagDTO> tags;
}

