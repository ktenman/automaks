package ee.tenman.automaks.controller;

import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import ee.tenman.automaks.service.TaxCalculationService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tax")
public class TaxCalculationController {

    @Resource
    private TaxCalculationService taxCalculationService;

    @PostMapping("/calculate")
    public Mono<ResponseEntity<TaxResponse>> calculateTax(@RequestBody @Valid CarDetails carDetails) {
        return taxCalculationService.calculateTax(carDetails).map(ResponseEntity::ok);
    }
}
