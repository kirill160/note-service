package org.example.noteservice;

import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;


import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteRepositoryWithTagsTest extends BaseRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tag tag1;
    private Tag tag2;
    private Tag tag3;
    private Note noteWithTags;

    @BeforeEach
    void setUp() {
        entityManager.clear();
        // 1. Создаём Note
        noteWithTags = Note.builder()
                .title("Заметка с тегами")
                .content("Текст заметки с тегами")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .tags(new HashSet<>())
                .build();

        entityManager.persist(noteWithTags);
        entityManager.flush();

        // 2. Создаём Tags
        tag1 = Tag.builder()
                .name("Важное")
                .build();

        tag2 = Tag.builder()
                .name("Срочное")
                .build();

        tag3 = Tag.builder()
                .name("Личное")
                .build();

        entityManager.persist(tag1);
        entityManager.persist(tag2);
        entityManager.persist(tag3);

        entityManager.flush();

        // 3. Создаём связь
        noteWithTags.getTags().add(tag1);
        noteWithTags.getTags().add(tag2);

        entityManager.flush();
    }

    @Test
    void save_shouldPersistNoteWithTags() {
        // Arrange
        Tag newTag = Tag.builder().name("Новый тег").build();
        entityManager.persist(newTag);

        Note newNote = Note.builder()
                .title("Новая заметка с тегом")
                .content("Текст")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .tags(Set.of(newTag))
                .build();

        // Act
        Note savedNote = noteRepository.save(newNote);
        entityManager.flush();

        // Assert
        assertThat(savedNote.getId()).isNotNull();
        assertThat(savedNote.getTags()).hasSize(1);
        assertThat(savedNote.getTags()).extracting(Tag::getName)
                .containsExactly("Новый тег");
    }

    @Test
    void findById_shouldReturnNoteWithTags() {
        // Act
        Note found = noteRepository.findById(noteWithTags.getId()).get();

        // Assert
        assertThat(found.getTags()).hasSize(2);
        assertThat(found.getTags()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Важное", "Срочное");
    }

    @Test
    void save_shouldUpdateNoteTags() {
        // Arrange
        Note note = noteRepository.findById(noteWithTags.getId()).get();

        note.setTags(new HashSet<>(Set.of(tag3)));// Меняем теги на "Личное"
        // Act
        Note updated = noteRepository.save(note);
        entityManager.flush();

        // Assert
        assertThat(updated.getTags()).hasSize(1);
        assertThat(updated.getTags()).extracting(Tag::getName)
                .containsExactly("Личное");
    }

    @Test
    void save_shouldAddMoreTagsToNote() {
        // Arrange
        Note note = noteRepository.findById(noteWithTags.getId()).get();
        Set<Tag> tags = new HashSet<>(Set.of(tag3));
        tags.addAll(note.getTags());
        note.setTags(tags);// Добавляем еще один тег

        // Act
        Note updated = noteRepository.save(note);
        entityManager.flush();

        // Assert
        assertThat(updated.getTags()).hasSize(3);
        assertThat(updated.getTags()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Важное", "Срочное", "Личное");
    }

    @Test
    void delete_shouldRemoveNoteWithTags() {
        // Arrange
        Long noteId = noteWithTags.getId();
        assertThat(noteRepository.existsById(noteId)).isTrue();

        tag1.setNotes(new HashSet<>());
        tag2.setNotes(new HashSet<>());

        // Act
        noteRepository.deleteById(noteId);
        entityManager.flush();

        // Assert
        assertThat(noteRepository.existsById(noteId)).isFalse();


    }

    @Test
    void findByTagsName_shouldReturnNotesWithMatchingTag() {
        // Act
        List<Note> results = noteRepository.findNotesByTags(List.of("Важное"));

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Заметка с тегами");
    }

    @Test
    void findByTagsNameIn_shouldReturnNotesWithAnyMatchingTag() {
        // Act - ищем заметки с тегами "Важное" или "Личное"
        List<Note> results = noteRepository.findNotesByTags(
                Arrays.asList("Важное", "Личное")
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Заметка с тегами");
    }

    @Test
    void findByTagsName_shouldReturnEmptyList_whenNoMatchingTag() {
        // Act
        List<Note> results = noteRepository.findNotesByTags(List.of("Несуществующий тег"));

        // Assert
        assertThat(results).isEmpty();
    }

}