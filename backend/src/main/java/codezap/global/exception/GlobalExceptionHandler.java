package codezap.global.exception;

import java.util.List;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleCodeZapException(CodeZapException codeZapException) {
        return ResponseEntity.status(codeZapException.getHttpStatusCode())
                .body(ProblemDetail.forStatusAndDetail(
                        codeZapException.getHttpStatusCode(),
                        codeZapException.getMessage())
                );
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        BindingResult bindingResult = exception.getBindingResult();
        List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        return ResponseEntity.badRequest()
                .body(ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        String.join("\n", errorMessages))
                );
    }

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleException(Exception exception) {
        return ResponseEntity.internalServerError()
                .body(ProblemDetail.forStatusAndDetail(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "서버에서 예상치 못한 오류가 발생하였습니다.")
                );
    }
}
