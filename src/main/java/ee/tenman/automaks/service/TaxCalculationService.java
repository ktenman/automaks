package ee.tenman.automaks.service;

import ee.tenman.automaks.config.aspect.Loggable;
import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class TaxCalculationService {

    @Loggable
    public Mono<TaxResponse> calculateTax(CarDetails carDetails) {

        TaxCalculator taxCalculator = TaxCalculatorFactory.getTaxCalculator(carDetails.getCarType());
        BigDecimal registrationTax = taxCalculator.calculateRegistrationTax(carDetails);
        BigDecimal annualTax = taxCalculator.calculateAnnualTax(carDetails);

        TaxResponse response = TaxResponse.builder()
                .annualTax(annualTax)
                .registrationTax(registrationTax)
                .build();

        return Mono.just(response);
    }

}
