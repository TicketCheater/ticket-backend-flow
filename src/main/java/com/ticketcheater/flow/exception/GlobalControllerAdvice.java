package com.ticketcheater.flow.exception;

import com.ticketcheater.flow.dto.response.Response;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Log4j2
@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(TicketApplicationException.class)
    Mono<ResponseEntity<?>> errorHandler(TicketApplicationException e) {
        log.error("Error occurs {}", e.toString());
        return Mono.just(ResponseEntity
                .status(e.getCode().getStatus())
                .body(Response.error(e.getCode().name())));
    }

}
