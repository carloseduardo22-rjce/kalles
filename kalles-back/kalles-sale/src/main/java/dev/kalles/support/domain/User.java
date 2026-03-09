package dev.kalles.support.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = "email")
public class User {

    private final String email;
    private final String name;

    public User(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
