package ee.tenman.automaks.service;

import ee.tenman.automaks.config.aspect.Loggable;
import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

import static ee.tenman.automaks.dto.CarDetails.CO2Type.NEDC;

@Service
@Slf4j
public class TaxCalculationService {

    private static final BigDecimal DISPLACEMENT_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal POWER_RATE = BigDecimal.valueOf(8.00);

    @Loggable
    public Mono<TaxResponse> calculateTax(CarDetails carDetails) {
        BigDecimal registrationTax = calculateRegistrationTax(carDetails);
        BigDecimal annualTax = calculateAnnualTax(carDetails);
        TaxResponse response = TaxResponse.builder()
                .annualTax(annualTax)
                .registrationTax(registrationTax)
                .build();
        return Mono.just(response);
    }

    private BigDecimal calculateRegistrationTax(CarDetails carDetails) {
        BigDecimal baseAmount = BigDecimal.valueOf(300);
        BigDecimal co2Component = calculateCO2Component(carDetails);
        BigDecimal massComponent = calculateMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        BigDecimal ageDiscountFactor = getAgeDiscountFactor(carDetails.getYear());
        BigDecimal discountedCO2AndMass = co2Component.add(massComponent).multiply(ageDiscountFactor);
        return baseAmount.add(discountedCO2AndMass);
    }

    private BigDecimal calculateAnnualTax(CarDetails carDetails) {
        BigDecimal baseAmount = BigDecimal.valueOf(50);
        BigDecimal co2Component = calculateAnnualCO2Component(carDetails.getCo2Emissions());
        BigDecimal massComponent = calculateAnnualMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        BigDecimal ageDiscountFactor = getAgeDiscountFactor(carDetails.getYear());
        BigDecimal discountedCO2AndMass = co2Component.add(massComponent).multiply(ageDiscountFactor);
        return baseAmount.add(discountedCO2AndMass);
    }

    private BigDecimal calculateCO2Component(CarDetails carDetails) {
        if (carDetails.isElectric()) {
            return BigDecimal.ZERO;
        }

        if (carDetails.getCo2Emissions() == null) {
            BigDecimal displacementComponent = BigDecimal.valueOf(carDetails.getEngineCapacity()).multiply(DISPLACEMENT_RATE);
            BigDecimal powerComponent = BigDecimal.valueOf(carDetails.getEnginePower()).multiply(POWER_RATE);
            return displacementComponent.add(powerComponent);
        }
        double co2Emissions = carDetails.getCo2Emissions();
        if (NEDC == carDetails.getCo2Type()) {
            co2Emissions = co2Emissions * 1.24;
        }
        return calculateTaxBasedOnEmissions(co2Emissions);
    }

    private BigDecimal calculateTaxBasedOnEmissions(double co2Emissions) {
        BigDecimal tax = BigDecimal.ZERO;
        if (co2Emissions > 200) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - 200, 80));
            co2Emissions = 200;
        }
        if (co2Emissions > 150) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - 150, 60));
            co2Emissions = 150;
        }
        if (co2Emissions > 117) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - 117, 40));
            co2Emissions = 117;
        }
        tax = tax.add(calculateTaxForEmissionRange(co2Emissions, 5));
        return tax;
    }

    private BigDecimal calculateTaxForEmissionRange(double emissionDifference, int rate) {
        return BigDecimal.valueOf(emissionDifference).multiply(BigDecimal.valueOf(rate));
    }

    private BigDecimal calculateMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? 2400 : 2000;
        if (fullMass <= massThreshold) {
            return BigDecimal.ZERO;
        }
        BigDecimal excessMass = BigDecimal.valueOf(fullMass - massThreshold);
        BigDecimal massTaxRate = BigDecimal.valueOf(4);
        return excessMass.multiply(massTaxRate);
    }

    private BigDecimal calculateAnnualCO2Component(Double co2Emissions) {
        if (co2Emissions == null || co2Emissions <= 117) {
            return BigDecimal.ZERO;
        } else if (co2Emissions <= 150) {
            return BigDecimal.valueOf(co2Emissions - 117).multiply(BigDecimal.valueOf(3));
        } else if (co2Emissions <= 200) {
            return BigDecimal.valueOf(co2Emissions - 150)
                    .multiply(BigDecimal.valueOf(3.5)).add(calculateAnnualCO2Component(150D));
        }
        return BigDecimal.valueOf(co2Emissions - 200)
                .multiply(BigDecimal.valueOf(4)).add(calculateAnnualCO2Component(200D));
    }

    private BigDecimal calculateAnnualMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? 2400 : 2000;
        BigDecimal excessMass = BigDecimal.valueOf(Math.max(0, fullMass - massThreshold));
        BigDecimal massTaxRate = BigDecimal.valueOf(0.4);
        BigDecimal massTaxCap = isElectric ? BigDecimal.valueOf(4400) : BigDecimal.valueOf(4000);
        BigDecimal massComponentTax = excessMass.multiply(massTaxRate);
        return massComponentTax.min(massTaxCap);
    }

    private BigDecimal getAgeDiscountFactor(int year) {
        int vehicleAge = LocalDate.now().getYear() - year;
        if (vehicleAge > 20) return BigDecimal.ZERO;
        if (vehicleAge > 15) return new BigDecimal("0.10");
        if (vehicleAge > 10) return new BigDecimal("0.50");
        if (vehicleAge > 5) return new BigDecimal("0.75");
        return BigDecimal.ONE;
    }

}
