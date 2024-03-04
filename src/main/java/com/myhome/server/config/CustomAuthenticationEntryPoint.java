package com.myhome.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@Configuration
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        int endPointResult = isEndPointExist(request);
        if(endPointResult == 1){
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        else if(endPointResult == -1){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    private int isEndPointExist(HttpServletRequest request){
        log.info("isEndPoint request url : {}", request.getRequestURI());
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        Set<RequestMappingInfo> requestMappingInfoSet = map.keySet();

        int result = -1; // 404

        for(RequestMappingInfo requestMappingInfo : requestMappingInfoSet){
            Set<String> directPaths = requestMappingInfo.getDirectPaths();
            if (!directPaths.isEmpty() && directPaths.contains(request.getRequestURI())) {
                Set<RequestMethod> methods = requestMappingInfo.getMethodsCondition().getMethods();
                if (!methods.isEmpty()) {
                    if(!methods.contains(RequestMethod.valueOf(request.getMethod()))) result = 1; // 405
                    else return 0; // 200
                }
            }
        }
        return result;
    }
}
