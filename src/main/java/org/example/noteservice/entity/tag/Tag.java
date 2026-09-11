package org.example.noteservice.entity.tag;

import jakarta.persistence.*;
import lombok.*;
import org.example.noteservice.entity.note.Note;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 50)
    private String name;
    @ManyToMany(mappedBy = "tags")
    private Set<Note> notes = new HashSet<>();
}
