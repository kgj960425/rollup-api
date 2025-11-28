package com.rollup.rollupapi.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.security.Principal;

@Getter
@AllArgsConstructor
public class FirebaseUserPrincipal implements Principal {
    private final String uid;
    private final String email;
    private final String displayName;

    @Override
    public String getName() {
        return uid;
    }
}
