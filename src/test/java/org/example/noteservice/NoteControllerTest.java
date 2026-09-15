package org.example.noteservice;


import org.example.noteservice.controller.NoteController;

import org.example.noteservice.dto.NoteRequestDTO;

import org.example.noteservice.dto.NoteResponseDTO;
import org.example.noteservice.dto.TagDTO;
import org.example.noteservice.handler.GlobalExceptionHandler;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.example.noteservice.service.NoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Import(GlobalExceptionHandler.class)
@WebMvcTest(NoteController.class)
public class NoteControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NoteService noteService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    void whenCreateNoteWithNullTextOfNote_thenReturn400() throws Exception {
        NoteRequestDTO expectedDto = new NoteRequestDTO("Заголовок заметки", null, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), false);
        mockMvc.perform(post("/api/note").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(expectedDto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists()).andExpect(jsonPath("$.errors.textOfNote").value("Текст не может быть пустым"));

        verify(noteService, never()).save(expectedDto);
    }

    @Test
    void whenCreateNoteWithNullTitle_thenReturn400() throws Exception {
        NoteRequestDTO expectedDto = new NoteRequestDTO(null, "Текст заметки", List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), false);
        mockMvc.perform(post("/api/note").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(expectedDto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists()).andExpect(jsonPath("$.errors.title").value("Заголовок не может быть пустым"));

        verify(noteService, never()).save(expectedDto);
    }

    @ParameterizedTest
    @MethodSource("invalidNoteDataProvider")
    void whenCreateNoteWithInvalidFields_thenReturn400(String testName, NoteRequestDTO dto, Map<String, String> expectedErrors) throws Exception {

        mockMvc.perform(post("/api/note").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").exists());

        for (Map.Entry<String, String> entry : expectedErrors.entrySet()) {
            mockMvc.perform(post("/api/note").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto))).andExpect(jsonPath("$.errors." + entry.getKey()).value(entry.getValue()));
        }

        verify(noteService, never()).save(any(NoteRequestDTO.class));
    }

    static Stream<Arguments> invalidNoteDataProvider() {
        return Stream.of(Arguments.of("Null textOfNote", new NoteRequestDTO("Заголовок", null, List.of(new TagDTO("Тег1")), false), Map.of("textOfNote", "Текст не может быть пустым")), Arguments.of("Empty textOfNote", new NoteRequestDTO("Заголовок", "", List.of(new TagDTO("Тег1")), false), Map.of("textOfNote", "Текст не может быть пустым")), Arguments.of("Null title", new NoteRequestDTO(null, "Текст заметки", List.of(new TagDTO("Тег1")), false), Map.of("title", "Заголовок не может быть пустым")), Arguments.of("Empty title", new NoteRequestDTO("", "Текст заметки", List.of(new TagDTO("Тег1")), false), Map.of("title", "Заголовок не может быть пустым")), Arguments.of("Empty tags", new NoteRequestDTO("Заголовок", "Текст заметки", List.of(), false), Map.of("tags", "must not be empty")), Arguments.of("Both null fields", new NoteRequestDTO(null, null, List.of(new TagDTO("Тег1")), false), Map.of("title", "Заголовок не может быть пустым", "textOfNote", "Текст не может быть пустым")));
    }

    @Test
    void whenCreateNoteFullFields_thenReturn200() throws Exception {
        NoteRequestDTO expectedDto = new NoteRequestDTO("Заголовок заметки", "Текст заметки", List.of(new TagDTO("Тег1"), new TagDTO("Тег2")), false);
        mockMvc.perform(post("/api/note").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(expectedDto))).andExpect(status().is2xxSuccessful()).andExpect(jsonPath("$.errors").doesNotExist());

        verify(noteService, times(1)).save(expectedDto);
    }

    @Test
    void whenFindNoteInvalidId_thenReturn404() throws Exception {
        Long noteId = 999L;
        when(noteService.findByIdDTO(noteId)).thenThrow(new NoteNotFoundException("Note with id 999 not found"));


        mockMvc.perform(get("/api/note/{id}", noteId)).andDo(res -> System.out.println("res " + res.getResponse().getContentAsString())).andDo(print()).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Note with id 999 not found"));

        verify(noteService, times(1)).findByIdDTO(noteId);

    }

    @Test
    void whenFindNoteValidId_thenReturn200() throws Exception {
        Long noteId = 1L;
        NoteResponseDTO expectedDto = new NoteResponseDTO(noteId, "Заголовок заметки", "Текст", false, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));
        when(noteService.findByIdDTO(noteId)).thenReturn(expectedDto);


        mockMvc.perform(get("/api/note/{id}", noteId)).andDo(res -> System.out.println("res " + res.getResponse().getContentAsString())).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.title").value("Заголовок заметки")).andExpect(jsonPath("$.text").value("Текст")).andExpect(jsonPath("$.archived").value(false)).andExpect(jsonPath("$.tags").isArray()).andExpect(jsonPath("$.tags.length()").value(2)).andExpect(jsonPath("$.tags[0].name").value("Тег1")).andExpect(jsonPath("$.tags[1].name").value("Тег2"));

        verify(noteService, times(1)).findByIdDTO(noteId);

    }

    @Test
    void whenDeleteNoteValidId_thenReturn204() throws Exception {

        Long noteId = 1L;
        doNothing().when(noteService).deleteById(noteId);


        mockMvc.perform(delete("/api/note/{id}", noteId)).andDo(print()).andExpect(status().isNoContent()).andExpect(content().string(""));

        verify(noteService, times(1)).deleteById(noteId);
    }

    @Test
    void whenDeleteNoteInvalidId_thenReturn404() throws Exception {
        Long invalidId = 999L;
        String errorMessage = "Note with id 999 not found";

        doThrow(new NoteNotFoundException(errorMessage)).when(noteService).deleteById(invalidId);

        mockMvc.perform(delete("/api/note/{id}", invalidId)).andDo(print()).andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value(errorMessage));

        verify(noteService, times(1)).deleteById(invalidId);
    }

    @Test
    void whenArchiveNoteWithValidIdAndArchiveTrue_thenReturn200() throws Exception {

        Long noteId = 1L;
        boolean archive = true;

        NoteResponseDTO expectedDto = new NoteResponseDTO(noteId, "Заголовок заметки", "Текст", true, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));

        when(noteService.updateArchiveStatus(noteId, archive)).thenReturn(expectedDto);

        mockMvc.perform(patch("/api/note/{id}?archive=true", noteId).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(noteId)).andExpect(jsonPath("$.title").value("Заголовок заметки")).andExpect(jsonPath("$.text").value("Текст")).andExpect(jsonPath("$.archived").value(true)).andExpect(jsonPath("$.tags").isArray()).andExpect(jsonPath("$.tags.length()").value(2)).andExpect(jsonPath("$.tags[0].name").value("Тег1")).andExpect(jsonPath("$.tags[1].name").value("Тег2"));


        verify(noteService, times(1)).updateArchiveStatus(noteId, archive);
    }

    @Test
    void whenUnarchiveNoteWithValidIdAndArchiveFalse_thenReturn200() throws Exception {
        Long noteId = 1L;
        boolean archive = false;

        NoteResponseDTO expectedDto = new NoteResponseDTO(noteId, "Заголовок заметки", "Текст", false, List.of(new TagDTO("Тег1"), new TagDTO("Тег2")));

        when(noteService.updateArchiveStatus(noteId, archive)).thenReturn(expectedDto);

        mockMvc.perform(patch("/api/note/{id}?archive=false", noteId).contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.id").value(noteId)).andExpect(jsonPath("$.archived").value(false));

        verify(noteService, times(1)).updateArchiveStatus(noteId, archive);
    }

    @Test
    void whenFindArchiveNotesWithArchiveTrue_thenReturnArchivedNotes() throws Exception {

        boolean archive = true;

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(1L, "Архивная заметка 1", "Текст 1", true, List.of()), new NoteResponseDTO(2L, "Архивная заметка 2", "Текст 2", true, List.of()));

        when(noteService.findNotesByArchive(archive)).thenReturn(expectedNotes);

        mockMvc.perform(get("/api/note/archive?findArchive=true").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].title").value("Архивная заметка 1")).andExpect(jsonPath("$[0].archived").value(true)).andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].title").value("Архивная заметка 2")).andExpect(jsonPath("$[1].archived").value(true));

        verify(noteService, times(1)).findNotesByArchive(archive);
    }

    @Test
    void whenFindArchiveNotesWithArchiveFalse_thenReturnNonArchivedNotes() throws Exception {

        boolean archive = false;

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(3L, "Активная заметка 1", "Текст 3", false, List.of()), new NoteResponseDTO(4L, "Активная заметка 2", "Текст 4", false, List.of()));

        when(noteService.findNotesByArchive(archive)).thenReturn(expectedNotes);

        mockMvc.perform(get("/api/note/archive?findArchive=false").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].archived").value(false)).andExpect(jsonPath("$[1].archived").value(false));


        verify(noteService, times(1)).findNotesByArchive(archive);
    }

    @Test
    void whenSearchBySingleTag_thenReturnNotesWithThatTag() throws Exception {

        List<String> tags = List.of("Важное");

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(1L, "Заметка с важным тегом", "Текст", false, List.of(new TagDTO("Важное"))), new NoteResponseDTO(2L, "Еще одна важная заметка", "Текст 2", false, List.of(new TagDTO("Важное"), new TagDTO("Срочное"))));

        when(noteService.searchByTag(tags)).thenReturn(expectedNotes);


        mockMvc.perform(get("/api/note/tags?nameTags=Важное").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].title").value("Заметка с важным тегом")).andExpect(jsonPath("$[0].tags[0].name").value("Важное")).andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].tags[0].name").value("Важное")).andExpect(jsonPath("$[1].tags[1].name").value("Срочное"));

        verify(noteService, times(1)).searchByTag(tags);
    }

    @Test
    void whenSearchByMultipleTags_thenReturnNotesWithAllTags() throws Exception {

        List<String> tags = Arrays.asList("Важное", "Срочное");

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(1L, "Заметка с важным и срочным тегами", "Текст", false, List.of(new TagDTO("Важное"), new TagDTO("Срочное"))));

        when(noteService.searchByTag(tags)).thenReturn(expectedNotes);

        mockMvc.perform(get("/api/note/tags?nameTags=Важное&nameTags=Срочное").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].tags.length()").value(2)).andExpect(jsonPath("$[0].tags[0].name").value("Важное")).andExpect(jsonPath("$[0].tags[1].name").value("Срочное"));

        verify(noteService, times(1)).searchByTag(tags);
    }

    @Test
    void whenSearchByTitleWithExactMatch_thenReturnNotes() throws Exception {

        String title = "Заметка";

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(1L, "Заметка о важном", "Текст 1", false, List.of(new TagDTO("Важное"))), new NoteResponseDTO(2L, "Заметка о срочном", "Текст 2", false, List.of(new TagDTO("Срочное"))));

        when(noteService.search(title)).thenReturn(expectedNotes);
        mockMvc.perform(get("/api/note/search?title=Заметка").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1L)).andExpect(jsonPath("$[0].title").value("Заметка о важном")).andExpect(jsonPath("$[1].id").value(2L)).andExpect(jsonPath("$[1].title").value("Заметка о срочном"));


        verify(noteService, times(1)).search(title);
    }

    @Test
    void whenSearchByTitleWithPartialMatch_thenReturnNotes() throws Exception {
        String title = "важн";

        List<NoteResponseDTO> expectedNotes = Arrays.asList(new NoteResponseDTO(1L, "Заметка о важном", "Текст", false, List.of(new TagDTO("Важное"))));

        when(noteService.search(title)).thenReturn(expectedNotes);

        mockMvc.perform(get("/api/note/search?title=важн").contentType(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].title").value("Заметка о важном"));

        verify(noteService, times(1)).search(title);
    }
}
