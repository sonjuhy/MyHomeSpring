package com.myhome.server.config.jwt;

import jakarta.servlet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String[] URL_JWT_LIST = {
            "/file/downloadPublicMedia/",
            "/file/downloadPublicImageLowQuality/",
            "/file/streamingPublicVideo/",
            "/file/downloadPrivateMedia/",
            "/file/downloadPrivateImageLowQuality/",
            "/file/streamingPrivateVideo/",
            "/file/downloadThumbNail/",
    };

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/auth/",
                "/swagger-ui/",
                "/v3/"
        };
        String path = request.getRequestURI();
        // 제외할 url 을 설정합니다.
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("doFilterInternal request url : {}", request.getRequestURI());
        String url = request.getRequestURI();
        if(checkURLJWTList(url)){
            String[] urlArr = url.split("/");
            String token = urlArr[urlArr.length - 1];
            if(jwtTokenProvider.validateToken(token)){
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
            }
            else{
                log.info("doFilterInternal token validate result is false");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        else{
            try{
                // 헤더에서 JWT 를 받아옵니다.
                String token = jwtTokenProvider.resolveToken(request);
                if(token != null){
                    // 유효한 토큰인지 확인합니다.
                    if (jwtTokenProvider.validateToken(token)) {
                        // 토큰이 유효하면 토큰으로부터 유저 정보를 받아옵니다.
                        Authentication authentication = jwtTokenProvider.getAuthentication(token);
                        // SecurityContext 에 Authentication 객체를 저장합니다.
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        filterChain.doFilter(request, response);
                    }
                    else {
                        log.info("doFilterInternal token validate result is false");
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
                else{
                    log.info("doFilterInternal token is null");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
            catch (Exception e){
                log.info("doFilterInternal exception");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
            }
        }
    }

    private boolean checkURLJWTList(String url){
        for(String list : URL_JWT_LIST){
            if(url.contains(list)) return true;
        }
        return false;
    }
}
