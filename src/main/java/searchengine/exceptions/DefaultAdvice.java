package searchengine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.ErrorResponse;

@ControllerAdvice
public class DefaultAdvice {
    @ExceptionHandler(SIteUrlIsEmptyOrNullException.class)
    public ResponseEntity<ErrorResponse> handleException(SIteUrlIsEmptyOrNullException e) {
        ErrorResponse response = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}

