package com.ticketcheater.flow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingResponse {
    private String queue;
    private Long userId;
    private Long rank;
}
