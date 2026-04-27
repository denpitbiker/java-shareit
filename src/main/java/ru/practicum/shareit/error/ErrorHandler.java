package ru.practicum.shareit.error;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    private static final String NOT_FOUND_ERR_LOG_MSG = "Not found error handled.";
    private static final String DUPLICATE_ERR_LOG_MSG = "Duplicate error handled.";
    private static final String FORBIDDEN_OPERATION_ERR_LOG_MSG = "Forbidden operation error handled.";
    private static final String VALIDATION_ERR_LOG_MSG = "Error while validating data handled.";
    private static final String UNKNOWN_ERR_LOG_MSG = "Unknown internal error handled.";
    private static final String UNKNOWN_ERR_MSG = "Unknown error happened.";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFound(NotFoundException e) {
        log.error(NOT_FOUND_ERR_LOG_MSG, e);
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDuplicatedData(ConflictException e) {
        log.error(DUPLICATE_ERR_LOG_MSG, e);
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleThrowable(Throwable e) {
        log.error(UNKNOWN_ERR_LOG_MSG, e);
        return new ErrorResponseDto(UNKNOWN_ERR_MSG);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleValidationException(ValidationException e) {
        log.error(VALIDATION_ERR_LOG_MSG, e);
        return new ErrorResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(VALIDATION_ERR_LOG_MSG, e);
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler({BadRequestException.class, MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto handleBadRequest(BadRequestException e) {
        log.error(VALIDATION_ERR_LOG_MSG, e);
        return new ErrorResponseDto(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseDto handleForbidden(ForbiddenOperationException e) {
        log.error(FORBIDDEN_OPERATION_ERR_LOG_MSG, e);
        return new ErrorResponseDto(e.getMessage());
    }
}
