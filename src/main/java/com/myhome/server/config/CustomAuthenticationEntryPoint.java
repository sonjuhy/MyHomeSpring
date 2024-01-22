package com.myhome.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@RequiredArgsConstructor
@Configuration
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final DispatcherServlet dispatcherServlet;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if(!isEndPointExist(request)){
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
        }
    }
    private boolean isEndPointExist(HttpServletRequest request){
        for(HandlerMapping handlerMapping : dispatcherServlet.getHandlerMappings()){
            try {
                HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
                if(handlerExecutionChain != null) return true;
            }
            catch (Exception e){
                return false;
            }
        }
        return false;
    }
}
