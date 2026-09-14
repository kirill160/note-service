package org.example.noteservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.example.noteservice.mapper.NoteMapperImpl;
import org.example.noteservice.repository.NoteArchiveRepository;
import org.example.noteservice.repository.NoteRepository;
import org.example.noteservice.repository.NoteSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteArchiveRepository noteArchiveRepository;
    private final NoteSearchRepository noteSearchRepository;
    private final NoteMapperImpl noteMapperImpl;
    private final TagService tagService;

    @Autowired
    public NoteService(NoteRepository noteRepository,
                       NoteArchiveRepository noteArchiveRepository,
                       NoteSearchRepository noteSearchRepository,
                       NoteMapperImpl noteMapperImpl,
                       TagService tagService
                       ) {
        this.noteRepository = noteRepository;
        this.noteArchiveRepository = noteArchiveRepository;
        this.noteSearchRepository = noteSearchRepository;
        this.noteMapperImpl = noteMapperImpl;
        this.tagService = tagService;

    }

    private Note findById(Long id) {
        return noteRepository
                .findById(id)
                .orElseThrow(() -> new NoteNotFoundException(String.format("Note with id %d not found", id)));
    }
    public NoteResponseDTO findByIdDTO(Long idDto){
        return noteMapperImpl.toNoteResponseDTO(findById(idDto));
    }

    @Transactional
    public NoteResponseDTO save(NoteRequestDTO noteRequestDTO) {
        List<Tag> tags = tagService.filterRequestTagsAndSaveFilteringTags(noteRequestDTO);
        Note saveNote = noteMapperImpl.toNote(noteRequestDTO);
        saveNote.setTags(new HashSet<>(tags));
        Note noteToDto = noteRepository.save(saveNote);
        return noteMapperImpl.toNoteResponseDTO(noteToDto);
    }
    @Transactional(timeout = 5, rollbackFor = {NoteNotFoundException.class})
    public void deleteById(Long id) {
        Note note = findById(id);
        noteRepository.delete(note);

    }
    @Transactional(timeout = 5, rollbackFor = {NoteNotFoundException.class})
    public NoteResponseDTO saveAndSetArchiveNote(Long id, boolean save) {
        Note note = findById(id);
        note.setArchive(save);
        return noteMapperImpl.toNoteResponseDTO(noteArchiveRepository.save(note));
    }

    public List<NoteResponseDTO> findNotesByArchive(boolean archive) {
        return noteMapperImpl.toNotesResponseDTO(noteArchiveRepository.getNotesByArchive(archive));
    }

    public List<NoteResponseDTO> searchByTag(List<String> tags) {
        return noteMapperImpl.toNotesResponseDTO(noteSearchRepository.findNotesByTags(tags));
    }

    public List<NoteResponseDTO> search(String searchString) {
        return noteMapperImpl.toNotesResponseDTO(noteSearchRepository.findNotesByQuery(searchString));
    }
}
