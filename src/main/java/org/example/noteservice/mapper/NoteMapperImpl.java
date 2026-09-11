package org.example.noteservice.mapper;

import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.dto.TagDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
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
        Set<Tag> tagSet = noteRequestDTO.getTags()
                .stream()
                .map(noteDTO -> Tag
                        .builder()
                        .name(noteDTO.getName())
                        .build()
                )
                .collect(Collectors.toSet());
        return Note.builder()
                .title(noteRequestDTO.getTitle())
                .content(noteRequestDTO.getTextOfNote())
                .archive(noteRequestDTO.isArchive())
                .tags(tagSet)
                .build();

    }

    @Override
    public List<NoteResponseDTO> toNotesResponseDTO(List<Note> notes) {
        return notes.stream()
                .map(this::toNoteResponseDTO)
                .collect(Collectors.toList());
    }

}
