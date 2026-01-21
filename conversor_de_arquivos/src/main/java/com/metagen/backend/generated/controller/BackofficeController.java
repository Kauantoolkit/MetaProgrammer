package com.metagen.backend.generated.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackofficeController {

    @GetMapping("/login")
    public String login() {
        return "backoffice/login";
    }

    @GetMapping("/backoffice")
    public String backofficeHome(Model model) {
        return "backoffice/index";
    }

}
