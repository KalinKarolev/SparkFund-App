package app.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("spark")
public class SparkController {

    @GetMapping
    public String getSparkPage() {
        return "spark";
    }

    @GetMapping("/edit")
    public String getEditSparkPage() {
        return "edit-spark";
    }

    @GetMapping("/all")
    public String getAllSparks() {
        return "all-sparks";
    }
}
