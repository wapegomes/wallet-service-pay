package com.walletservice.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record SignupRequest(
    @NotBlank
    @Size(min = 3, max = 20)
    String username,

    @NotBlank
    @Size(max = 50)
    @Email
    String email,

    @NotBlank
    @Size(min = 6, max = 40)
    String password,

    Set<String> roles
) {}
