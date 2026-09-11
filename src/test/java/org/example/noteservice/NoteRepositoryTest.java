package org.example.noteservice;




import org.example.noteservice.entity.note.Note;
import org.example.noteservice.repository.NoteArchiveRepository;
import org.example.noteservice.repository.NoteRepository;
import org.example.noteservice.repository.NoteSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteArchiveRepository noteArchiveRepository;

    @Autowired
    private NoteSearchRepository noteSearchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Note testNote1;
    private Note testNote2;
    private Note archivedNote;

    @BeforeEach
    void setUp() {

        entityManager.clear();


        testNote1 = Note.builder()
                .title("Заметка о важном")
                .content("Это очень важная заметка")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();

        testNote2 = Note.builder()
                .title("Заметка о срочном")
                .content("Это срочная заметка")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();

        archivedNote = Note.builder()
                .title("Архивная заметка")
                .content("Эта заметка в архиве")
                .archive(true)
                .createdAt(OffsetDateTime.now())
                .build();


        entityManager.persist(testNote1);
        entityManager.persist(testNote2);
        entityManager.persist(archivedNote);
        entityManager.flush();
    }

    // ========== CRUD ТЕСТЫ ==========

    @Test
    void save_shouldPersistNote() {

        Note newNote = Note.builder()
                .title("Новая заметка")
                .content("Текст новой заметки")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();


        Note savedNote = noteRepository.save(newNote);


        assertThat(savedNote).isNotNull();
        assertThat(savedNote.getId()).isNotNull();
        assertThat(savedNote.getTitle()).isEqualTo("Новая заметка");
        assertThat(savedNote.getContent()).isEqualTo("Текст новой заметки");
        assertThat(savedNote.isArchive()).isFalse();
        assertThat(savedNote.getCreatedAt()).isNotNull(); // Если есть аудит



        Note found = entityManager.find(Note.class, savedNote.getId());
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Новая заметка");
    }

    @Test
    void save_shouldPersistNoteWithArchiveTrue() {

        Note archivedNote = Note.builder()
                .title("Архивная заметка")
                .content("Текст архивной заметки")
                .archive(true)
                .createdAt(OffsetDateTime.now())
                .build();


        Note saved = noteRepository.save(archivedNote);


        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isArchive()).isTrue();
    }

    @Test
    void findById_shouldReturnNote_whenExists() {

        Optional<Note> found = noteRepository.findById(testNote1.getId());


        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Заметка о важном");
        assertThat(found.get().getContent()).isEqualTo("Это очень важная заметка");
        assertThat(found.get().isArchive()).isFalse();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {

        Optional<Note> found = noteRepository.findById(999L);


        assertThat(found).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllNotes() {

        List<Note> notes = (List<Note>) noteRepository.findAll();


        assertThat(notes).hasSize(3);
        assertThat(notes).extracting(Note::getTitle)
                .containsExactlyInAnyOrder(
                        "Заметка о важном",
                        "Заметка о срочном",
                        "Архивная заметка"
                );
    }

    @Test
    void findAll_shouldReturnEmpty_whenNoNotes() {

        noteRepository.deleteAll();


        List<Note> notes = (List<Note>) noteRepository.findAll();


        assertThat(notes).isEmpty();
    }

    @Test
    void update_shouldUpdateNote() {

        Note noteToUpdate = noteRepository.findById(testNote1.getId()).get();
        noteToUpdate.setTitle("Обновленный заголовок");
        noteToUpdate.setContent("Обновленный текст");
        noteToUpdate.setArchive(true);


        Note updatedNote = noteRepository.save(noteToUpdate);
        entityManager.flush();


        assertThat(updatedNote.getTitle()).isEqualTo("Обновленный заголовок");
        assertThat(updatedNote.getContent()).isEqualTo("Обновленный текст");
        assertThat(updatedNote.isArchive()).isTrue();


        Note found = entityManager.find(Note.class, testNote1.getId());
        assert found != null;
        assertThat(found.getTitle()).isEqualTo("Обновленный заголовок");
        assertThat(found.isArchive()).isTrue();
    }

    @Test
    void delete_shouldRemoveNote() {

        Long noteId = testNote2.getId();
        assertThat(noteRepository.existsById(noteId)).isTrue();


        noteRepository.deleteById(noteId);
        entityManager.flush();


        assertThat(noteRepository.existsById(noteId)).isFalse();
        assertThat(noteRepository.findAll()).hasSize(2);
    }

    @Test
    void delete_shouldRemoveNoteEntity() {

        Note note = noteRepository.findById(testNote1.getId()).get();


        noteRepository.delete(note);
        entityManager.flush();


        assertThat(noteRepository.existsById(testNote1.getId())).isFalse();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {

        assertThat(noteRepository.existsById(testNote1.getId())).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotFound() {

        assertThat(noteRepository.existsById(999L)).isFalse();
    }

    @Test
    void count_shouldReturnNumberOfNotes() {

        assertThat(noteRepository.count()).isEqualTo(3);
    }

    // ========== КАСТОМНЫЕ ЗАПРОСЫ ==========

    @Test
    void findByArchive_shouldReturnArchivedNotes() {

        List<Note> archivedNotes = noteArchiveRepository.getNotesByArchive(true);


        assertThat(archivedNotes).hasSize(1);
        assertThat(archivedNotes.get(0).getTitle()).isEqualTo("Архивная заметка");
        assertThat(archivedNotes.get(0).isArchive()).isTrue();
    }

    @Test
    void findByArchive_shouldReturnActiveNotes() {

        List<Note> activeNotes = noteArchiveRepository.getNotesByArchive(false);


        assertThat(activeNotes).hasSize(2);
        assertThat(activeNotes).allMatch(note -> !note.isArchive());
        assertThat(activeNotes).extracting(Note::getTitle)
                .containsExactlyInAnyOrder("Заметка о важном", "Заметка о срочном");
    }

    @Test
    void searchByTitle_shouldReturnNotesWithMatchingTitle() {

        List<Note> results = noteSearchRepository.findNotesByQuery("важн");


        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Заметка о важном");
    }

    @Test
    void searchByTitle_shouldReturnMultipleNotes_whenMultipleMatch() {

        List<Note> results = noteSearchRepository.findNotesByQuery("Заметка");


        assertThat(results).hasSize(3);
        assertThat(results).extracting(Note::getTitle)
                .containsExactlyInAnyOrder("Заметка о важном", "Заметка о срочном", "Архивная заметка");
    }

    @Test
    void searchByTitle_shouldReturnEmptyList_whenNoMatch() {

        List<Note> results = noteSearchRepository.findNotesByQuery("Несуществующее");


        assertThat(results).isEmpty();
    }

    @Test
    void searchByTitle_shouldBeCaseInsensitive() {

        List<Note> results = noteSearchRepository.findNotesByQuery("заметка");

        assertThat(results).hasSize(3);
    }


}
