package no.oslomet.gatewayservice.controller;

import no.oslomet.gatewayservice.model.Error;
import no.oslomet.gatewayservice.model.User;
import no.oslomet.gatewayservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
@RequestMapping("/errors")
@ControllerAdvice
public class MyErrorController implements ErrorController {

    @Autowired
    private UserService userService;

    public MyErrorController() {}

    @ExceptionHandler(HttpClientErrorException.class)
    public String handleDatabaseServerError(Model model, final HttpClientErrorException exception) {
        Error error = new Error();
        appendExceptionInformation(error, exception);
        model.addAttribute("error", error);

        Optional<User> user = getUserSession(model, SecurityContextHolder.getContext().getAuthentication(), userService);
        if(user.isPresent()) {
            model.addAttribute("user", user.get());
        }

        return "index";
    }

    @GetMapping("/error")
    public String handleClientServerError(Model model, HttpServletRequest request) {
        Error error = new Error();
        appendClientErrorInformation(error, request, "Internal server error: Unknown error", "500");
        model.addAttribute("error", error);

        Optional<User> user = getUserSession(model, SecurityContextHolder.getContext().getAuthentication(), userService);
        if(user.isPresent()) {
            model.addAttribute("user", user.get());
        }

        return "index";
    }

    @GetMapping("/credentialError")
    public String handleClientCredentialError(Model model, HttpServletRequest request) {
        Error error = new Error();
        appendClientErrorInformation(error, request, "Unauthorized: Invalid username or password", "401");
        model.addAttribute("error", error);
        return "index";
    }

    @GetMapping("/forbiddenError")
    public String handleClientForbiddenError(Model model, HttpServletRequest request) {
        Error error = new Error();
        appendClientErrorInformation(error, request, "Forbidden: Forbidden action attempted", "403");
        model.addAttribute("error", error);
        return "index";
    }

    @Override
    public String getErrorPath() {
        return "";
    }

    private void appendExceptionInformation(Error error, HttpClientErrorException exception) {
        int statusCode = exception.getStatusCode().value();
        error.setCode(Integer.toString(statusCode));

        String reasonPhrase = exception.getStatusCode().getReasonPhrase();
        error.setMessage(reasonPhrase);
    }

    private void appendClientErrorInformation(Error error, HttpServletRequest request, String message, String errorCode) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object statusMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if(statusCode != null) {
            error.setCode(statusCode.toString());
        } else {
            error.setCode(errorCode);
        }

        if(statusMessage != null) {
            error.setMessage(statusMessage.toString());
        } else {
            error.setMessage(message);
        }
    }

    private Optional<User> getUserSession(Model model, Authentication auth, UserService userService) {
        return userService.getUserByEmail(auth.getName());
    }
}