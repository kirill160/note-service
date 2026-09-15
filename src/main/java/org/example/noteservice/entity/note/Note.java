package org.example.noteservice.entity.note;

import jakarta.persistence.*;
import lombok.*;
import org.example.noteservice.entity.tag.Tag;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity(name = "notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"tags", "id"} )
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title",  nullable = false)
    private String title;
    @Column(name = "content", nullable = false, length = 1000)
    private String content;
    @Column(
            nullable = false,
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;
    @Column
    private boolean archive;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})

    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
