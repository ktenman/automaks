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
    private static final BigDecimal BASE_REGISTRATION_AMOUNT = BigDecimal.valueOf(300);
    private static final BigDecimal BASE_ANNUAL_AMOUNT = BigDecimal.valueOf(50);
    private static final Double CO2_CONVERSION_FACTOR_NEDC = 1.24;
    private static final int ELECTRIC_MASS_THRESHOLD = 2400;
    private static final int NON_ELECTRIC_MASS_THRESHOLD = 2000;
    private static final BigDecimal MASS_TAX_RATE = BigDecimal.valueOf(4);
    private static final BigDecimal ANNUAL_MASS_TAX_RATE = BigDecimal.valueOf(0.4);
    private static final BigDecimal ELECTRIC_MASS_TAX_CAP = BigDecimal.valueOf(4400);
    private static final BigDecimal NON_ELECTRIC_MASS_TAX_CAP = BigDecimal.valueOf(4000);
    private static final int VEHICLE_AGE_DISCOUNT_THRESHOLD_20 = 20;
    private static final int VEHICLE_AGE_DISCOUNT_THRESHOLD_15 = 15;
    private static final int VEHICLE_AGE_DISCOUNT_THRESHOLD_10 = 10;
    private static final int VEHICLE_AGE_DISCOUNT_THRESHOLD_5 = 5;
    private static final BigDecimal AGE_DISCOUNT_OVER_15 = new BigDecimal("0.10");
    private static final BigDecimal AGE_DISCOUNT_OVER_10 = new BigDecimal("0.50");
    private static final BigDecimal AGE_DISCOUNT_OVER_5 = new BigDecimal("0.75");
    private static final Double CO2_THRESHOLD_200 = 200D;
    private static final Double CO2_THRESHOLD_150 = 150D;
    private static final Double CO2_THRESHOLD_117 = 117D;
    private static final int EMISSION_RATE_200_PLUS = 80;
    private static final int EMISSION_RATE_150_TO_200 = 60;
    private static final int EMISSION_RATE_117_TO_150 = 40;
    private static final int EMISSION_RATE_UP_TO_117 = 5;
    private static final BigDecimal CO2_LOW_EMISSION_RATE = BigDecimal.valueOf(3);
    private static final BigDecimal CO2_MEDIUM_EMISSION_RATE = BigDecimal.valueOf(3.5);
    private static final BigDecimal CO2_HIGH_EMISSION_RATE = BigDecimal.valueOf(4);

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
        BigDecimal co2Component = calculateCO2Component(carDetails);
        BigDecimal massComponent = calculateMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        BigDecimal ageDiscountFactor = getAgeDiscountFactor(carDetails.getYear());
        BigDecimal discountedCO2AndMass = co2Component.add(massComponent).multiply(ageDiscountFactor);
        return BASE_REGISTRATION_AMOUNT.add(discountedCO2AndMass);
    }

    private BigDecimal calculateAnnualTax(CarDetails carDetails) {
        BigDecimal co2Component = calculateAnnualCO2Component(carDetails.getCo2Emissions());
        BigDecimal massComponent = calculateAnnualMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        BigDecimal ageDiscountFactor = getAgeDiscountFactor(carDetails.getYear());
        BigDecimal discountedCO2AndMass = co2Component.add(massComponent).multiply(ageDiscountFactor);
        return BASE_ANNUAL_AMOUNT.add(discountedCO2AndMass);
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
            co2Emissions = co2Emissions * CO2_CONVERSION_FACTOR_NEDC;
        }
        return calculateTaxBasedOnEmissions(co2Emissions);
    }

    private BigDecimal calculateTaxBasedOnEmissions(double co2Emissions) {
        BigDecimal tax = BigDecimal.ZERO;
        if (co2Emissions > CO2_THRESHOLD_200) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - CO2_THRESHOLD_200, EMISSION_RATE_200_PLUS));
            co2Emissions = CO2_THRESHOLD_200;
        }
        if (co2Emissions > CO2_THRESHOLD_150) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - CO2_THRESHOLD_150, EMISSION_RATE_150_TO_200));
            co2Emissions = CO2_THRESHOLD_150;
        }
        if (co2Emissions > CO2_THRESHOLD_117) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions - CO2_THRESHOLD_117, EMISSION_RATE_117_TO_150));
            co2Emissions = CO2_THRESHOLD_117;
        }
        tax = tax.add(calculateTaxForEmissionRange(co2Emissions, EMISSION_RATE_UP_TO_117));
        return tax;
    }

    private BigDecimal calculateTaxForEmissionRange(double emissionDifference, int rate) {
        return BigDecimal.valueOf(emissionDifference).multiply(BigDecimal.valueOf(rate));
    }

    private BigDecimal calculateMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? ELECTRIC_MASS_THRESHOLD : NON_ELECTRIC_MASS_THRESHOLD;
        if (fullMass <= massThreshold) {
            return BigDecimal.ZERO;
        }
        BigDecimal excessMass = BigDecimal.valueOf(fullMass - massThreshold);
        return excessMass.multiply(MASS_TAX_RATE);
    }

    private BigDecimal calculateAnnualCO2Component(Double co2Emissions) {
        if (co2Emissions == null || co2Emissions <= CO2_THRESHOLD_117) {
            return BigDecimal.ZERO;
        } else if (co2Emissions <= CO2_THRESHOLD_150) {
            return BigDecimal.valueOf(co2Emissions - CO2_THRESHOLD_117).multiply(CO2_LOW_EMISSION_RATE);
        } else if (co2Emissions <= CO2_THRESHOLD_200) {
            return BigDecimal.valueOf(co2Emissions - CO2_THRESHOLD_150)
                    .multiply(CO2_MEDIUM_EMISSION_RATE).add(calculateAnnualCO2Component(CO2_THRESHOLD_150));
        }
        return BigDecimal.valueOf(co2Emissions - CO2_THRESHOLD_200)
                .multiply(CO2_HIGH_EMISSION_RATE).add(calculateAnnualCO2Component(CO2_THRESHOLD_200));
    }

    private BigDecimal calculateAnnualMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? ELECTRIC_MASS_THRESHOLD : NON_ELECTRIC_MASS_THRESHOLD;
        BigDecimal excessMass = BigDecimal.valueOf(Math.max(0, fullMass - massThreshold));
        BigDecimal massTaxCap = isElectric ? ELECTRIC_MASS_TAX_CAP : NON_ELECTRIC_MASS_TAX_CAP;
        BigDecimal massComponentTax = excessMass.multiply(ANNUAL_MASS_TAX_RATE);
        return massComponentTax.min(massTaxCap);
    }
    private BigDecimal getAgeDiscountFactor(int year) {
        int vehicleAge = LocalDate.now().getYear() - year;
        if (vehicleAge > VEHICLE_AGE_DISCOUNT_THRESHOLD_20) return BigDecimal.ZERO;
        if (vehicleAge > VEHICLE_AGE_DISCOUNT_THRESHOLD_15) return AGE_DISCOUNT_OVER_15;
        if (vehicleAge > VEHICLE_AGE_DISCOUNT_THRESHOLD_10) return AGE_DISCOUNT_OVER_10;
        if (vehicleAge > VEHICLE_AGE_DISCOUNT_THRESHOLD_5) return AGE_DISCOUNT_OVER_5;
        return BigDecimal.ONE;
    }

}
