package training.common.web;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.NoSuchElementException;
@RestControllerAdvice
public class Errors {
 @ExceptionHandler(NoSuchElementException.class) ProblemDetail missing(NoSuchElementException e) { return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage()); }
 @ExceptionHandler({IllegalArgumentException.class,MethodArgumentNotValidException.class}) ProblemDetail invalid(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Invalid request: "+e.getMessage()); }
 @ExceptionHandler({IllegalStateException.class,DataIntegrityViolationException.class}) ProblemDetail conflict(Exception e) { return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Operation conflicts with current state"); }
}
