package com.elearning.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;


    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if(header == null || !header.startsWith("Bearer ")) {{
            filterChain.doFilter(request, response);
            return ;
        }}
        String token = header.substring(7);
         try {
             Claims claims = jwtService.extractClaims(token);
             String email = jwtService.extractEmail(token);
             String role = claims.get("role", String.class);

             var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

             var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

             SecurityContextHolder.getContext().setAuthentication(authentication);

         }
         catch (JwtException e) {
             SecurityContextHolder.clearContext();
         }

        // Dù token hợp lệ hay không, request PHẢI được đi tiếp tới bước sau —
        // filter này chỉ có nhiệm vụ set/clear authentication, không tự quyết
        // định chặn request (việc đó là của SecurityConfig).
        filterChain.doFilter(request, response);
    }
}
