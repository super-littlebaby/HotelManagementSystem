package com.project.hotelmanagementsystem.interceptor;

import com.project.hotelmanagementsystem.annotation.RequiresRoles;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.service.AuthService;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.isEmpty()) {
            return true;
        }
        
        Employee employee = authService.getEmployeeByToken(token);
        
        if (employee != null) {
            request.setAttribute("employee", employee);
            request.setAttribute("employeeHotelId", employee.getHotelId());
            request.setAttribute("token", token);
            
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
        
        // 员工token验证失败，尝试验证客人token
        Guest guest = authService.getGuestByToken(token);
        if (guest != null) {
            request.setAttribute("guest", guest);
            return true;
        }
        
        return handleNoAuth(response);
    }
    
    private boolean handleNoAuth(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":401,\"msg\":\"未授权\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
        writer.flush();
        writer.close();
        return false;
    }
    
    private boolean handleForbidden(HttpServletResponse response) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":403,\"msg\":\"权限不足\",\"data\":null,\"timestamp\":" + System.currentTimeMillis() + "}");
        writer.flush();
        writer.close();
        return false;
    }
}