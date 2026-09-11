package org.example.noteservice.conroller;

import jakarta.validation.Valid;
import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/note")
public class NoteController {
    private final NoteService service;

    @Autowired
    public NoteController(NoteService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Cacheable(value="note", key="#id")
    public ResponseEntity<NoteResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findByIdDTO(id));
    }

    @PostMapping()
    public ResponseEntity<NoteResponseDTO> create(@Valid @RequestBody NoteRequestDTO noteRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(noteRequestDTO));

    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "note", key = "#id")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    @Caching(
            put = { @CachePut(value = "note", key = "#id") },
            evict = { @CacheEvict(value = "note", key = "#id") }
    )
    public ResponseEntity<NoteResponseDTO> updateSetArchiveAndSave(@PathVariable Long id, @RequestParam(required = false, name = "archive") boolean archive) {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(service.saveAndSetArchiveNote(id, archive));
    }

    @GetMapping("/archive")
    public ResponseEntity<List<NoteResponseDTO>> findByArchive(@RequestParam(required = false, name = "findArchive") boolean archive) {
        return ResponseEntity.status(HttpStatus.OK).body(service.findNotesByArchive(archive));

    }

    @GetMapping("/tags")
    public ResponseEntity<List<NoteResponseDTO>> getTags(@Valid @RequestParam(required = false, name = "nameTags") List<String> tags) {
        return ResponseEntity.status(HttpStatus.OK).body(service.searchByTag(tags));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponseDTO>> search(@Valid @RequestParam(required = false, name = "title") String title) {
        return ResponseEntity.status(HttpStatus.OK).body(service.search(title));
    }
}
