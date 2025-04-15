package app.web;

import app.exceptions.DomainException;
import app.exceptions.EmailAlreadyExistException;
import app.exceptions.ResourceNotFoundException;
import app.exceptions.UsernameAlreadyExistException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.MissingResourceException;

@ControllerAdvice
public class ExceptionAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            NoResourceFoundException.class,
            TypeMismatchException.class,
            MethodArgumentTypeMismatchException.class,
            AuthorizationDeniedException.class,
            MissingResourceException.class,
            MissingRequestValueException.class,
            ResourceNotFoundException.class,
    })
    public ModelAndView handleNotFoundException() {
        return new ModelAndView("not-found-error");
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class
            , DomainException.class
            , DuplicateKeyException.class})
    public ModelAndView handleAccessDeniedException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exceptionMessage", ex.getMessage());
        modelAndView.setViewName("access-denied-error");
        return modelAndView;
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public String handleUsernameAlreadyExistException(RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String username = request.getParameter("username");
        redirectAttributes.addFlashAttribute("usernameAlreadyExistMessage", "Username %s already exist".formatted(username));
        return "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExist(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("emailAlreadyExistMessage", "This email already exist");
        return "redirect:/register";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception ex) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("exceptionName", ex.getClass().getSimpleName());
        modelAndView.setViewName("generic-error");
        return modelAndView;
    }

}
