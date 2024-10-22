package dev.dankoz.BaseServer.config;

import dev.dankoz.BaseServer.auth.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver handlerExceptionResolver;

    public JwtAuthenticationFilter(TokenService tokenService, UserDetailsService userDetailsService, HandlerExceptionResolver handlerExceptionResolver) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludedPaths = new String[] {"auth/login","auth/register","auth/refresh"};
        return Arrays.stream(excludedPaths)
                .anyMatch(e -> new AntPathMatcher().match(e,request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            filterChain.doFilter(request,response);
            return;
        }

        Optional<Cookie> cookie = Arrays.stream(cookies).filter(e -> e.getName().equals("jwtToken")).findFirst();
        if(cookie.isEmpty()){
            filterChain.doFilter(request,response);
            return;
        }

        String jwt = cookie.get().getValue();
        String email = null;

        try {
             email = tokenService.extractUsername(jwt);
        }catch (ExpiredJwtException e){
            handlerExceptionResolver.resolveException(request,response,null,new ExpiredJwtException(e.getHeader(),e.getClaims(),"JWT Token expired"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(email != null && authentication == null){
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

            if(tokenService.isTokenValid(jwt,userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails
                        ,null
                        ,userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
            filterChain.doFilter(request,response);
    }
}
