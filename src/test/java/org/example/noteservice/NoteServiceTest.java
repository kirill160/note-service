package org.example.noteservice;

import org.example.noteservice.dto.NoteRequestDTO;
import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.dto.TagDTO;
import org.example.noteservice.entity.note.Note;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.example.noteservice.mapper.NoteMapperImpl;
import org.example.noteservice.repository.NoteRepository;
import org.example.noteservice.repository.TagRepository;
import org.example.noteservice.service.NoteService;
import org.example.noteservice.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private NoteMapperImpl noteMapper;
    @InjectMocks
    private NoteService noteService;
    @Mock
    private TagService tagService;



    private Note note;
    private NoteRequestDTO noteRequestDTO;
    private NoteResponseDTO noteResponseDTO;
    private Note note1;
    private NoteResponseDTO noteResponseDTO1;


    @BeforeEach
    public void setUp() {
        note = Note.builder()
                .archive(true)
                .content("Контент заметки")
                .createdAt(OffsetDateTime.now())
                .id(1L)
                .title("Заголовок первой заметки")
                .build();
        noteRequestDTO = new NoteRequestDTO("Заголовок первой заметки", "Контент заметки", List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), true);
        noteResponseDTO = new NoteResponseDTO(1L, "Заголовок первой заметки", "Контент заметки", true, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));
        note1 = Note.builder()
                .archive(true)
                .content("Заметка для поиска")
                .title("Заголовок заметки поиска")
                .createdAt(OffsetDateTime.now())
                .id(2L)
                .build();
        noteResponseDTO1 = new NoteResponseDTO(2L, "Заголовок заметки поиска", "Заметка для поиска", true, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));
        NoteRequestDTO noteRequestDTO1 = new NoteRequestDTO("Заголовок заметки поиска", "Заметка для поиска", List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), true);
    }

    //сервис
    @Test
    void whenPostNoteWithValidData_thenReturnNoteDto() {
        when(noteMapper.toNote(any(NoteRequestDTO.class))).thenReturn(note);
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        when(noteMapper.toNoteResponseDTO(any(Note.class))).thenReturn(noteResponseDTO);

        NoteResponseDTO noteResponseDTOTest = noteService.save(noteRequestDTO);
        assertNotNull(noteResponseDTOTest);
        assertEquals(1L, noteResponseDTOTest.getId());
        assertEquals("Заголовок первой заметки", noteResponseDTOTest.getTitle());
        assertEquals("Контент заметки", noteResponseDTOTest.getText());
        assertTrue(noteResponseDTOTest.isArchived());
        assertEquals(List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), noteResponseDTOTest.getTags());

        verify(noteMapper, times(1)).toNote(any(NoteRequestDTO.class));
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(noteMapper, times(1)).toNoteResponseDTO(any(Note.class));
    }

    @Test
    void whenPostNoteData_thenReturnNotEqualsWithInvalidData() {
        when(noteMapper.toNote(any(NoteRequestDTO.class))).thenReturn(note);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        when(noteMapper.toNoteResponseDTO(any(Note.class))).thenReturn(noteResponseDTO);

        NoteResponseDTO noteResponseDTOTest = noteService.save(noteRequestDTO);
        assertNotNull(noteResponseDTOTest);
        assertNotEquals(2L, noteResponseDTOTest.getId());
        assertNotEquals("Заголовок второй заметки", noteResponseDTOTest.getTitle());
        assertNotEquals("Контент второй заметки", noteResponseDTOTest.getText());
        assertTrue(noteResponseDTOTest.isArchived());
        assertNotEquals(List.of(new TagDTO("Тег2"), new TagDTO("Тег1")), noteResponseDTOTest.getTags());

        verify(noteRepository, times(1)).save(any(Note.class));
        verify(noteMapper, times(1)).toNoteResponseDTO(any(Note.class));
        verify(noteMapper, times(1)).toNote(any(NoteRequestDTO.class));
    }

    @Test
    void whenFindNoteById_thenReturnNote() {
        when(noteRepository.findById(eq(2L))).thenReturn(Optional.of(note1));
        when(noteMapper.toNoteResponseDTO(any(Note.class))).thenReturn(noteResponseDTO1);

        NoteResponseDTO noteResponseDTOTest = noteService.findByIdDTO(2L);
        assertNotNull(noteResponseDTOTest);
        assertEquals(2L, noteResponseDTOTest.getId());
        assertEquals("Заголовок заметки поиска", noteResponseDTOTest.getTitle());
        assertEquals("Заметка для поиска", noteResponseDTOTest.getText());
        assertTrue(noteResponseDTOTest.isArchived());

        verify(noteMapper, times(1)).toNoteResponseDTO(any(Note.class));
        verify(noteRepository, times(1)).findById(eq(2L));
    }

    @Test
    void whenFindNoteByInvalidId_thenReturnNotFound() {
        assertThrows(NoteNotFoundException.class, () -> noteService.findByIdDTO(999L));
        verify(noteMapper, never()).toNoteResponseDTO(any(Note.class));
        verify(noteRepository, times(1)).findById(anyLong());
    }

    @Test
    void whenDeleteNoteById_thenReturnVoid() {

        doNothing().when(noteRepository).deleteById(1L);
        noteService.deleteById(1L);

        verify(noteRepository, times(1)).deleteById(eq(1L));


    }

    @Test
    void whenFindNoteByIdAndSetArchived_thenReturnNote() {
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));
        when(noteMapper.toNoteResponseDTO(any(Note.class))).thenReturn(noteResponseDTO);
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteResponseDTO noteResponseDTOTest = noteService.updateArchiveStatus(1L, true);
        assertNotNull(noteResponseDTOTest);
        assertEquals(1L, noteResponseDTOTest.getId());
        assertEquals("Заголовок первой заметки", noteResponseDTOTest.getTitle());
        assertEquals("Контент заметки", noteResponseDTOTest.getText());
        assertTrue(noteResponseDTOTest.isArchived());

        verify(noteMapper, times(1)).toNoteResponseDTO(any(Note.class));
        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void whenFindNoteByIdAndSetArchivedWithNotValidData_thenReturnNote() {
        Note noteForTest = Note.builder()
                .id(1L)
                .title("Заголовок первой невалидной заметки")
                .content("Контент невалидной заметки")
                .archive(true)
                .build();


        Note expectedNote = Note.builder()
                .id(1L)
                .title("Заголовок первой невалидной заметки")
                .content("Контент невалидной заметки")
                .archive(false)  // ← После сохранения будет false
                .build();


        NoteResponseDTO expectedDto = new NoteResponseDTO(
                1L,
                "Заголовок первой невалидной заметки",
                "Контент невалидной заметки",
                false,
                List.of(new TagDTO("Тег1"), new TagDTO("Тег2"))
        );


        when(noteRepository.findById(1L)).thenReturn(Optional.of(noteForTest));
        when(noteRepository.save(any(Note.class))).thenReturn(expectedNote);
        when(noteMapper.toNoteResponseDTO(any(Note.class))).thenReturn(expectedDto);

        NoteResponseDTO result = noteService.updateArchiveStatus(1L, false);

        assertNotNull(result);
        assertNotEquals(999L, result.getId());
        assertFalse(result.isArchived());

        verify(noteRepository, times(1)).findById(1L);
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(noteMapper, times(1)).toNoteResponseDTO(any(Note.class));

    }

    @Test
    void whenFindNotesByArchiveFalse_thenReturnNonArchivedNotes() {
        Note note = Note.builder()
                .archive(false)
                .content("Заметка для поиска")
                .title("Заголовок заметки поиска")
                .createdAt(OffsetDateTime.now())
                .id(2L)
                .build();
        NoteResponseDTO expectedDto = new NoteResponseDTO(2L, "Заголовок заметки поиска", "Заметка для поиска", false, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));
        boolean archive = false;
        List<Note> notes = List.of(note);

        when(noteRepository.getNotesByArchive(archive)).thenReturn(notes);
        when(noteMapper.toNotesResponseDTO(notes)).thenReturn(List.of(expectedDto));

        List<NoteResponseDTO> result = noteService.findNotesByArchive(archive);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Заголовок заметки поиска", result.get(0).getTitle());
        assertFalse(result.get(0).isArchived());

        verify(noteRepository, times(1)).getNotesByArchive(archive);
        verify(noteMapper, times(1)).toNotesResponseDTO(notes);
    }

    @Test
    void whenFindNotesByArchiveTrue_thenReturnArchivedNotes() {
        Note note = Note.builder()
                .archive(true)
                .content("Заметка для поиска")
                .title("Заголовок заметки поиска")
                .createdAt(OffsetDateTime.now())
                .id(2L)
                .build();
        NoteResponseDTO expectedDto = new NoteResponseDTO(2L, "Заголовок заметки поиска", "Заметка для поиска", true, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));
        boolean archive = false;
        List<Note> notes = List.of(note);  // только note2 архивная

        when(noteRepository.getNotesByArchive(archive)).thenReturn(notes);
        when(noteMapper.toNotesResponseDTO(notes)).thenReturn(List.of(expectedDto));

        List<NoteResponseDTO> result = noteService.findNotesByArchive(archive);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Заголовок заметки поиска", result.get(0).getTitle());
        assertTrue(result.get(0).isArchived());

        verify(noteRepository, times(1)).getNotesByArchive(archive);
        verify(noteMapper, times(1)).toNotesResponseDTO(notes);
    }

    @Test
    void whenSearchByTagsWithValidTags_thenReturnNotes() {
        List<String> tags = List.of("java", "spring");

        Note note = Note.builder()
                .id(1L)
                .title("Java и Spring")
                .content("Учим Spring Boot")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();

        List<Note> notes = List.of(note);

        NoteResponseDTO expectedDto = new NoteResponseDTO(1L, "Java и Spring", "Учим Spring Boot", false, List.of(new TagDTO("java"), new TagDTO("spring")));

        List<NoteResponseDTO> expectedDtos = List.of(expectedDto);

        when(noteRepository.findNotesByTags(tags)).thenReturn(notes);
        when(noteMapper.toNotesResponseDTO(notes)).thenReturn(expectedDtos);

        List<NoteResponseDTO> result = noteService.searchByTag(tags);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java и Spring", result.get(0).getTitle());
        assertEquals(2, result.get(0).getTags().size());

        verify(noteRepository, times(1)).findNotesByTags(tags);
        verify(noteMapper, times(1)).toNotesResponseDTO(notes);
    }

    @Test
    void whenSearchByTagsWithEmptyList_thenReturnEmptyList() {
        List<String> emptyTags = Collections.emptyList();

        when(noteRepository.findNotesByTags(emptyTags))
                .thenReturn(Collections.emptyList());

        when(noteMapper.toNotesResponseDTO(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<NoteResponseDTO> result = noteService.searchByTag(emptyTags);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(noteRepository, times(1)).findNotesByTags(emptyTags);
        verify(noteMapper, times(1)).toNotesResponseDTO(Collections.emptyList());
    }

    @Test
    void whenSearchWithValidQuery_thenReturnNotes() {
        String query = "PostgreSQL";

        Note note1 = Note.builder()
                .id(1L)
                .title("PostgreSQL настройка")
                .content("Настройка PostgreSQL для полнотекстового поиска")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();

        Note note2 = Note.builder()
                .id(2L)
                .title("Spring Boot поиск")
                .content("Как настроить поиск в Spring Boot")
                .archive(false)
                .createdAt(OffsetDateTime.now())
                .build();

        List<Note> notes = List.of(note1, note2);

        NoteResponseDTO dto1 = new NoteResponseDTO(1L, "PostgreSQL настройка", "Настройка PostgreSQL для полнотекстового поиска", false, List.of(new TagDTO("java"), new TagDTO("spring")));

        NoteResponseDTO dto2 = new NoteResponseDTO(2L, "Spring Boot поиск", "Как настроить поиск в Spring Boot", false, List.of(new TagDTO("java"), new TagDTO("spring")));

        List<NoteResponseDTO> expectedDtos = List.of(dto1, dto2);

        when(noteRepository.findNotesByQuery(query)).thenReturn(notes);
        when(noteMapper.toNotesResponseDTO(notes)).thenReturn(expectedDtos);

        List<NoteResponseDTO> result = noteService.search(query);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PostgreSQL настройка", result.get(0).getTitle());
        assertEquals("Spring Boot поиск", result.get(1).getTitle());

        verify(noteRepository, times(1)).findNotesByQuery(query);
        verify(noteMapper, times(1)).toNotesResponseDTO(notes);
    }



}
