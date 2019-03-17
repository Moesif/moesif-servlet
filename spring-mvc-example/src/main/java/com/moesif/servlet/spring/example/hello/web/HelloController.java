package  com.moesif.servlet.spring.example.hello.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloController {

    @RequestMapping(value = "/api/json", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String json(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "{\n" +
                "    \"id\": 1,\n" +
                "    \"content\": \"Hello, World! You are 5\"\n" +
                "}";
    }

    @RequestMapping(value = "/api/html", method = RequestMethod.GET)
    public ModelAndView html(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return new ModelAndView("hello");
    }
}
