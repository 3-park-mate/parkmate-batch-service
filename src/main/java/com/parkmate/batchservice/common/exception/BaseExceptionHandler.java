package com.parkmate.batchservice.common.exception;

import com.parkmate.batchservice.common.response.ApiResponse;
import com.parkmate.batchservice.common.response.ResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<Object>> BaseError(BaseException e) {

        HttpStatus status = e.getStatus().getHttpStatus();

        if (ResponseStatus.RESOURCE_NOT_FOUND.equals(e.getStatus())) {
            return new ResponseEntity<>(
                    ApiResponse.error(e.getStatus()),
                    HttpStatus.OK
            );
        }

        return new ResponseEntity<>(
                ApiResponse.error(e.getStatus()),
                status
        );
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiResponse<Object>> RuntimeError(RuntimeException e) {

        BaseException response = new BaseException(ResponseStatus.INTERNAL_SERVER_ERROR);
        for (StackTraceElement s : e.getStackTrace()) {
            System.out.println(s);
        }

        return new ResponseEntity<>(
                ApiResponse.error(response.getStatus()),
                response.getStatus().getHttpStatus()
        );
    }
}
