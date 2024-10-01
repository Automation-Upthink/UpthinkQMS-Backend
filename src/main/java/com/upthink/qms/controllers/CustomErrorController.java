package com.upthink.qms.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        System.out.println("Error occurred: Status Code = " + status);
        if (exception != null) {
            System.out.println("Exception: " + exception.getMessage());
        }

        // You can return a specific view here, or a generic error message.
        return "error";  // Return an error view
    }
}

