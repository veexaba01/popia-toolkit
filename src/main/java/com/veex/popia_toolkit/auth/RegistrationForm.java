package com.veex.popia_toolkit.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm
{
    @NotBlank @Size(max = 200)
    private String organisationName;

    @NotBlank @Email @Size(max = 254)
    private String email;

    @NotBlank @Size(min = 12, max = 72)
    private String password;

    public String getOrganisationName() { return organisationName; }
    public void setOrganisationName(String organisationName) { this.organisationName = organisationName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString()
    {
        return "RegistrationForm[organisationName=" + organisationName + ", email=" + email + ", password=***]";
    }
}