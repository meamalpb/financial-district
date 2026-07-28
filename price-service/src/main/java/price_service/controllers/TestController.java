package price_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import price_service.models.Price;
import price_service.repositories.PriceRepository;

@RestController
@RequestMapping("/test")
public class TestController {

    private final PriceRepository repository;

    public TestController(PriceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Price test() {

        Price price = new Price();
        price.setSymbol("SPLG");
        price.setPrice(74.32);

        return repository.save(price);
    }
}