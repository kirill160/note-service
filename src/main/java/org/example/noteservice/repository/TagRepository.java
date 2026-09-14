package org.example.noteservice.repository;

import org.example.noteservice.entity.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT DISTINCT t FROM tags t WHERE t.name IN :names ORDER BY t.id")
    List<Tag> findExistsTagByNames (@Param("names") List<String> tagsName);
}
