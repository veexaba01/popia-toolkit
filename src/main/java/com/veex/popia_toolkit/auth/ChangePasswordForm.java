package com.veex.popia_toolkit.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordForm
{
    @NotBlank @Size(max = 72)
    private String currentPassword;

    @NotBlank @Size(min = 12, max = 72)
    private String newPassword;

    @NotBlank @Size(max = 72)
    private String confirmPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    @Override
    public String toString()
    {
        return "ChangePasswordForm[currentPassword=***, newPassword=***, confirmPassword=***]";
    }
}