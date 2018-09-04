package loans.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class LoanValidationException extends RuntimeException {

    public LoanValidationException() {
    }

    public LoanValidationException(String message) {
        super(message);
    }

}
