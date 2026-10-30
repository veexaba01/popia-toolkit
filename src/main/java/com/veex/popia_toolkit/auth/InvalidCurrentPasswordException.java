package com.veex.popia_toolkit.auth;

public class InvalidCurrentPasswordException extends RuntimeException
{
    public InvalidCurrentPasswordException()
    {
        super("The current password is not correct");
    }
}