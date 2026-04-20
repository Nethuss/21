package ru.didcvee.ecd.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.didcvee.ecd.security.jwt.access.AccessTokenFacade;
import ru.didcvee.ecd.security.service.UserDetailsImpl;

import java.io.IOException;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private AccessTokenFacade accessTokenFacade;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);

            if (jwt != null) {

                if (!accessTokenFacade.validate(jwt)) {
                    throw new BadCredentialsException("Invalid JWT token");
                }

                UserDetailsImpl userDetails = accessTokenFacade.parse(jwt);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {
                // Токена нет — просто не аутентифицируем пользователя (анонимный)
            }
        } catch (BadCredentialsException ex) {
            SecurityContextHolder.clearContext();
            throw ex;
        } catch (Exception ex) {
            // Любые другие ошибки парсинга/валидации -> считаем как BadCredentials
            throw new BadCredentialsException("Failed to process JWT token", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        return accessTokenFacade.getJwtFromCookies(request);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        return path.startsWith("/auth/") || path.startsWith("/docs") || path.startsWith("/api");
    }
}
