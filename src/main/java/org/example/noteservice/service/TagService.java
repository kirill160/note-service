package org.example.noteservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.TagDTO;
import org.example.noteservice.entity.tag.Tag;
import org.example.noteservice.repository.TagRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    private final TagRepository repository;

    private List<Tag> getTagsFromRequestDTO(NoteRequestDTO noteRequestDTO) {
        List<String> namesTag = noteRequestDTO.getTags()
                .stream()
                .map(TagDTO::getName)
                .toList();
        return repository.findExistsTagByNames(namesTag);
    }
    public List<Tag> filterRequestTagsAndSaveFilteringTags(NoteRequestDTO noteRequestDTO) {
        Set<Tag> tags = mapToTags(noteRequestDTO);
        return repository.saveAll(tags);
    }
    private Set<Tag> mapToTags(NoteRequestDTO noteRequestDTO) {
        Set<TagDTO> existsTags = getTagsFromRequestDTO(noteRequestDTO)
                .stream()
                .map(tag -> new TagDTO(tag.getName()))
                .collect(Collectors.toSet());
        return noteRequestDTO
                .getTags()
                .stream()
                .filter(tagDTO -> !existsTags.contains(tagDTO))
                .map(tagDTO -> Tag.builder().name(tagDTO.getName()).build())
                .collect(Collectors.toSet());
    }
}
