package com.ticketcheater.flow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaitingDTO {
    private String queue;
    private Long userId;
    private Long rank;

    public static WaitingDTO of(String queue, Long userId, Long rank) {
        return new WaitingDTO(queue, userId, rank);
    }

}
