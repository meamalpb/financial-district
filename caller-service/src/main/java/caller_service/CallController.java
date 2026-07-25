package caller_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CallController {

    private final RestTemplate restTemplate;

    public CallController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/call")
    public String call() {

        return restTemplate.getForObject(
                "http://dummy-service/hello",
                String.class
        );
    }
}