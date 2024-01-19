package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class TaxCalculationService {
    
    public Mono<TaxResponse> calculateTax(CarDetails carDetails) {

        TaxCalculator taxCalculator = TaxCalculatorFactory.getTaxCalculator(carDetails.getCarType());
        BigDecimal registrationTax = taxCalculator.calculateRegistrationTax(carDetails);
        BigDecimal annualTax = taxCalculator.calculateAnnualTax(carDetails);

        TaxResponse response = new TaxResponse(registrationTax, annualTax);

        return Mono.just(response);
    }

}
