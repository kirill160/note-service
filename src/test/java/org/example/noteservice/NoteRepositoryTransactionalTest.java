package org.example.noteservice;

import org.example.noteservice.entity.note.Note;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class NoteRepositoryTransactionalTest extends BaseRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Note noteWithTags;

    @BeforeEach
    void setUp() {
        entityManager.clear();

        Tag tag = Tag.builder().name("Тег").build();
        entityManager.persist(tag);

        noteWithTags = Note.builder()
                .title("Заметка с тегом")
                .content("Текст")
                .archive(false)
                .tags(Set.of(tag))
                .createdAt(OffsetDateTime.now())
                .build();

        entityManager.persist(noteWithTags);
        entityManager.flush();
    }

    @Test
    @Transactional
    void findById_shouldLoadTagsLazily() {
        // Act
        Note found = noteRepository.findById(noteWithTags.getId()).get();

        // Assert - теги подгрузятся благодаря @Transactional
        assertThat(found.getTags()).hasSize(1);
        assertThat(found.getTags()).extracting(Tag::getName)
                .containsExactly("Тег");
    }

    @Test
    @Transactional
    void save_shouldUpdateNoteAndTags() {
        // Arrange
        Note found = noteRepository.findById(noteWithTags.getId()).get();

        // Act - изменяем внутри транзакции
        found.setTitle("Обновленная заметка");

        // Assert
        Note updated = noteRepository.findById(noteWithTags.getId()).get();
        assertThat(updated.getTitle()).isEqualTo("Обновленная заметка");
        assertThat(updated.getTags()).hasSize(1);
    }
}