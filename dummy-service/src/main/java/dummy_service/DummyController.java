package dummy_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DummyController {
    
    @GetMapping("/hello")
    public String hello(){
        return "hello from dummy";
    }
}
