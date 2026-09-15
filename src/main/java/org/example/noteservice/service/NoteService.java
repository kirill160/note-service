package org.example.noteservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.example.noteservice.mapper.NoteMapperImpl;
import org.example.noteservice.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteMapperImpl noteMapperImpl;
    private final TagService tagService;

    @Autowired
    public NoteService(NoteRepository noteRepository,
                       NoteMapperImpl noteMapperImpl,
                       TagService tagService
                       ) {
        this.noteRepository = noteRepository;
        this.noteMapperImpl = noteMapperImpl;
        this.tagService = tagService;

    }

    private Note findById(Long id) {
        return noteRepository
                .findById(id)
                .orElseThrow(() -> new NoteNotFoundException(String.format("Note with id %d not found", id)));
    }
    @Cacheable(cacheNames = "notes", key ="#idDto")
    public NoteResponseDTO findByIdDTO(Long idDto){
        return noteMapperImpl.toNoteResponseDTO(findById(idDto));
    }
    @Transactional
    @CachePut(cacheNames = "notes", key = "#result.id")
    public NoteResponseDTO save(NoteRequestDTO noteRequestDTO) {
        List<Tag> tags = tagService.filterRequestTagsAndSaveFilteringTags(noteRequestDTO);
        Note saveNote = noteMapperImpl.toNote(noteRequestDTO);
        saveNote.setTags(new HashSet<>(tags));
        Note noteToDto = noteRepository.save(saveNote);
        return noteMapperImpl.toNoteResponseDTO(noteToDto);
    }

    @CacheEvict(cacheNames = "notes", key = "#id")
    @Transactional(timeout = 5, rollbackFor = {NoteNotFoundException.class})
    public void deleteById(Long id) {
            noteRepository.deleteById(id);
    }

    @CachePut(cacheNames = "notes", key = "#id" )
    @Transactional(timeout = 5, rollbackFor = {NoteNotFoundException.class})
    public NoteResponseDTO updateArchiveStatus(Long id, boolean save) {
        Note note = findById(id);
        note.setArchive(save);
        return noteMapperImpl.toNoteResponseDTO(noteRepository.save(note));
    }
    @Transactional(readOnly = true)
    public List<NoteResponseDTO> findNotesByArchive(boolean archive) {
        return noteMapperImpl.toNotesResponseDTO(noteRepository.getNotesByArchive(archive));
    }
    @Transactional(readOnly = true)
    public List<NoteResponseDTO> searchByTag(List<String> tags) {
        return noteMapperImpl.toNotesResponseDTO(noteRepository.findNotesByTags(tags));
    }
    @Transactional(readOnly = true)
    public List<NoteResponseDTO> search(String searchString) {
        return noteMapperImpl.toNotesResponseDTO(noteRepository.findNotesByQuery(searchString));
    }
}
