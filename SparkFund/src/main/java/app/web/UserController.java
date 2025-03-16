package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class UserController {

    @GetMapping("users")
    public String getAllUsersPage() {
        return "users";
    }

    @GetMapping("profile")
    public String getUserProfilePage() {
        return "profile";
    }

}
