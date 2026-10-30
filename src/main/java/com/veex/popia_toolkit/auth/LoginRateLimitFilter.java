package com.veex.popia_toolkit.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter
{
    private final LoginAttemptTracker tracker;

    public LoginRateLimitFilter(LoginAttemptTracker tracker)
    {
        this.tracker = tracker;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException
    {
        if (isLoginPost(request) && tracker.isBlocked(request.getParameter("username")))
        {
            response.sendRedirect(request.getContextPath() + "/login?error");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isLoginPost(HttpServletRequest request)
    {
        return "POST".equals(request.getMethod())
                && request.getRequestURI().equals(request.getContextPath() + "/login");
    }
}