package org.example.noteservice.repository;

import org.example.noteservice.entity.note.Note;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends CrudRepository<Note, Long> {
    List<Note> getNotesByArchive(Boolean archive);
    @Query("SELECT DISTINCT n FROM notes n JOIN n.tags t WHERE t.name IN :tags GROUP BY n")
    List<Note> findNotesByTags(@Param("tags") List<String> tags);

    @Query(nativeQuery = true, value = """
                                        SELECT id, title, content, created_at, archive, ts_rank(search_vector, query) AS RANK 
                                        FROM notes, plainto_tsquery('russian', :nameTitleAndContent) AS QUERY 
                                        WHERE search_vector @@ query 
                                        ORDER BY rank DESC
                                        """)
    List<Note> findNotesByQuery(@Param("nameTitleAndContent") String nameTitleAndContent);
}
