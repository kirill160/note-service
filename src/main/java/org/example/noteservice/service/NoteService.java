package org.example.noteservice.service;

import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.example.noteservice.mapper.NoteMapperImpl;
import org.example.noteservice.repository.NoteArchiveRepository;
import org.example.noteservice.repository.NoteRepository;
import org.example.noteservice.repository.NoteSearchRepository;
import org.example.noteservice.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final NoteArchiveRepository noteArchiveRepository;
    private final NoteSearchRepository noteSearchRepository;
    private final NoteMapperImpl noteMapperImpl;
    pr
    private final Logger logger = Logger.getLogger(NoteService.class.getName());

    @Autowired
    public NoteService(NoteRepository noteRepository,
                       NoteArchiveRepository noteArchiveRepository,
                       NoteSearchRepository noteSearchRepository,
                       NoteMapperImpl noteMapperImpl
                       ) {
        this.noteRepository = noteRepository;
        this.noteArchiveRepository = noteArchiveRepository;
        this.noteSearchRepository = noteSearchRepository;
        this.noteMapperImpl = noteMapperImpl;

    }

    private Note findById(Long id) {
        return noteRepository
                .findById(id)
                .orElseThrow(() -> new NoteNotFoundException(String.format("Note with id %d not found", id)));
    }
    public NoteResponseDTO findByIdDTO(Long idDto){
        return noteMapperImpl.toNoteResponseDTO(findById(idDto));
    }
    public NoteResponseDTO save(NoteRequestDTO noteRequestDTO ) {
        Note note = noteMapperImpl.toNote(noteRequestDTO);
        return noteMapperImpl.toNoteResponseDTO(noteRepository.save(note));
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
