package org.example.noteservice.handler;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.noteservice.handler.exception.NoteNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String NOT_FOUND_TITLE = "Note Not Found";
    private static final String BAD_REQUEST_TITLE = "Bad Request";

    private ProblemDetail problemDetail(HttpServletRequest request, Exception ex, final HttpStatus status, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setTitle(title);
        return problemDetail;
    }

    private ProblemDetail problemDetailForValidate(HttpServletRequest request, Exception ex, final HttpStatus status, final Map<String, String> properties, String title) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("errors", properties);
        problemDetail.setTitle(title);
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
                .collect(Collectors.toMap(FieldError::getField, (error) -> error.getDefaultMessage() == null ? "Validation error" : error.getDefaultMessage(),
                        (existing, replacement) -> existing + "; " + replacement ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetailForValidate(request, ex, HttpStatus.BAD_REQUEST, errors, BAD_REQUEST_TITLE));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException ex) {
        showLog(ex, request, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail(request, ex, HttpStatus.BAD_REQUEST, BAD_REQUEST_TITLE));
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleMethodValidationException(HttpServletRequest request, HandlerMethodValidationException ex) {
        showLog(ex, request, HttpStatus.BAD_REQUEST);
        String message = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(";"));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setTitle(BAD_REQUEST_TITLE);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(HttpServletRequest request, MethodArgumentTypeMismatchException ex) {
        showLog(ex, request, HttpStatus.BAD_REQUEST);
        String message = String.format("Invalid boolean request parameter type: %s", ex.getValue());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create(""));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setTitle(BAD_REQUEST_TITLE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);

    }



}
