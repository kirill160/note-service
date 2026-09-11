package org.example.noteservice.mapper;

import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.entity.note.Note;

import java.util.List;

public interface NoteMapper {
    NoteResponseDTO toNoteResponseDTO(Note note);
    Note toNote(NoteRequestDTO noteRequestDTO);
    List<NoteResponseDTO> toNotesResponseDTO(List<Note> notes);
}
