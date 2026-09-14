package org.example.noteservice.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.dto.TagDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.handler.exception.TagAlreadyExistsException;
import org.example.noteservice.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoteMapperImpl implements NoteMapper {
    @Override
    public NoteResponseDTO toNoteResponseDTO(Note note) {
        List<TagDTO> tagsDTOs = note.getTags()
                .stream()
                .map(notes -> new TagDTO(notes.getName()))
                .collect(Collectors.toList());
        return new NoteResponseDTO(note.getId(), note.getTitle(), note.getContent(), note.isArchive(), tagsDTOs);
    }

    @Override
    public Note toNote(NoteRequestDTO noteRequestDTO) {
        return Note.builder()
                .title(noteRequestDTO.getTitle())
                .content(noteRequestDTO.getTextOfNote())
                .archive(noteRequestDTO.isArchive())
                .build();

    }

    @Override
    public List<NoteResponseDTO> toNotesResponseDTO(List<Note> notes) {
        return notes.stream()
                .map(this::toNoteResponseDTO)
                .collect(Collectors.toList());
    }

}
