package org.example.noteservice.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String NOT_FOUND_TITLE = "Note Not Found";

    private ProblemDetail problemDetail(HttpServletRequest request, Exception ex, final HttpStatus status, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setTitle(title);
        return problemDetail;
    }

    private ProblemDetail problemDetailForValidate(HttpServletRequest request, Exception ex, final HttpStatus status, final Map<String, String> properties) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errors", properties);
        return problemDetail;
    }

    private void showLog(Exception ex, HttpServletRequest request, HttpStatus status) {
        log.error("Response error: status={}, path={}, message={}",
                status, request.getRequestURI(), ex.getMessage(), ex);
    }

    @ExceptionHandler(NoteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ProblemDetail> handleNoteNotFoundException(HttpServletRequest request, NoteNotFoundException ex) {
        showLog(ex, request, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail(request, ex, HttpStatus.NOT_FOUND, NOT_FOUND_TITLE));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        showLog(ex, request, HttpStatus.BAD_REQUEST);
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, (error) -> {
                assert error.getDefaultMessage() != null;
                return error.getDefaultMessage();
                }));
        errors.forEach((k,v) -> System.out.println("error: " + k + ": " + v));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetailForValidate(request, ex, HttpStatus.BAD_REQUEST, errors));
    }


}
