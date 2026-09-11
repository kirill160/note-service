package org.example.noteservice.repository;

import org.example.noteservice.entity.note.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteArchiveRepository extends JpaRepository<Note, Long> {
    List<Note> getNotesByArchive(Boolean archive);
}
