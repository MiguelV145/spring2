package ec.edu.ups.icc.fundamentos01.exceptions.domain;

import org.springframework.http.HttpStatus;

import ec.edu.ups.icc.fundamentos01.exceptions.base.ApplicationException;

public class BusinessException extends ApplicationException {

    public BusinessException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }

    protected BusinessException(HttpStatus status, String message) {
        super(status, message);
    }
}