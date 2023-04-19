package fi.vm.yti.groupmanagement.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class DefaultController {

    @GetMapping(
        value = {        
        "/**/{path:[^.]*}"
    },
    produces = "text/html; charset=UTF-8")
    @ResponseBody
    ClassPathResource defaultPage() {
        return new ClassPathResource("/static/index.html");
    }
}
