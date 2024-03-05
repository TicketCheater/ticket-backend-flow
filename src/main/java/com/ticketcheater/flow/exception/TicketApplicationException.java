package com.ticketcheater.flow.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketApplicationException extends RuntimeException {

    private ErrorCode code;
    private String message;

}
