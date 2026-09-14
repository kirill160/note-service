package org.example.noteservice;

import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TagDatabaseTest extends BaseRepositoryTest{
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    public void setup() {
        testEntityManager.clear();
        Tag tagOne = Tag.builder()
                .name("Тег1")
                .build();
        Tag tagTwo = Tag.builder()
                        .name("Тег2")
                        .build();
        Tag tagThree = Tag.builder()
                        .name("Тег3")
                        .build();
        testEntityManager.persist(tagOne);
        testEntityManager.persist(tagTwo);
        testEntityManager.persist(tagThree);
        testEntityManager.flush();
    }

    @Test
    void findByTags_shouldReturnTags(){
        List<Tag> tags = tagRepository.findExistsTagByNames(List.of("Тег1", "Тег2", "Тег3"));

        assertThat(tags).hasSize(3);
        assertThat(tags).extracting(Tag::getName)
                .containsExactly("Тег1", "Тег2", "Тег3");
    }

    @Test
    void findByTags_shouldNotReturnDuplicates_whenInputHasDuplicates() {
        List<Tag> tags = tagRepository.findExistsTagByNames(List.of("Тег1", "Тег1", "Тег2"));
        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Тег1", "Тег2");
    }
    @Test
    void findByTags_shouldReturnEmpty_whenTagDoesNotExist() {
        List<Tag> tags = tagRepository.findExistsTagByNames(List.of("Несуществующий"));
        assertThat(tags).isEmpty();
    }
    @Test
    void findByTags_shouldReturnOnlyExisting_whenSomeDoNotExist() {
        List<Tag> tags = tagRepository.findExistsTagByNames(List.of("Тег1", "Несуществующий", "Тег2"));
        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Тег1", "Тег2");
    }
    @Test
    void findByTags_shouldReturnEmpty_whenInputIsEmpty() {
        List<Tag> tags = tagRepository.findExistsTagByNames(List.of());
        assertThat(tags).isEmpty();
    }



}
