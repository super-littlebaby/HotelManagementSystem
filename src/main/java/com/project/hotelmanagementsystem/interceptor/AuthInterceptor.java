package com.project.hotelmanagementsystem.interceptor;

import com.project.hotelmanagementsystem.annotation.RequiresRoles;
import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            return handleNoAuth(response);
        }
        
        Employee employee = authService.getEmployeeByToken(token);
        
        if (employee == null) {
            return handleNoAuth(response);
        }
        
        request.setAttribute("employee", employee);
        request.setAttribute("employeeHotelId", employee.getHotelId());
        
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequiresRoles requiresRoles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
            
            if (requiresRoles != null) {
                String[] allowedRoles = requiresRoles.value();
                String employeeRole = employee.getRole();
                
                if (!Arrays.asList(allowedRoles).contains(employeeRole)) {
                    return handleForbidden(response);
                }
            }
        }
        
        return true;
    }
    
    private boolean handleNoAuth(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(ResponseResult.error(401, "未授权")));
        writer.flush();
        writer.close();
        return false;
    }
    
    private boolean handleForbidden(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(ResponseResult.error(403, "权限不足")));
        writer.flush();
        writer.close();
        return false;
    }
}