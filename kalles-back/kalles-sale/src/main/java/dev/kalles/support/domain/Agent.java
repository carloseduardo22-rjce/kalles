package dev.kalles.support.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "employeeId")
public class Agent {

    private final String employeeId;
    private final String name;

    public Agent(String employeeId, String name) {
        this.employeeId = employeeId;
        this.name = name;
    }
}
