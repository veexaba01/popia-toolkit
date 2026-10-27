package com.veex.popia_toolkit.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = 200) String organisationName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 72) String password)
{
    @Override
    public String toString()
    {
        return "RegistrationRequest[organisationName=" + organisationName
                + ", email=" + email + ", password=***]";
    }
}