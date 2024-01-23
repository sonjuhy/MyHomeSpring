package com.myhome.server.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@RequiredArgsConstructor
@Configuration
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final DispatcherServlet dispatcherServlet;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
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
