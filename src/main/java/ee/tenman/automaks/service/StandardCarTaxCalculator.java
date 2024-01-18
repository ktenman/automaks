package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;

import java.math.BigDecimal;
import java.time.LocalDate;

import static ee.tenman.automaks.dto.CarDetails.CO2Type.NEDC;
import static org.apache.commons.lang3.compare.ComparableUtils.is;

public class StandardCarTaxCalculator implements TaxCalculator {

    private static final BigDecimal DISPLACEMENT_RATE = BigDecimal.valueOf(0.05);
    private static final BigDecimal POWER_RATE = BigDecimal.valueOf(8.00);
    private static final BigDecimal BASE_REGISTRATION_AMOUNT = BigDecimal.valueOf(300);
    private static final BigDecimal CO2_CONVERSION_FACTOR_NEDC = BigDecimal.valueOf(1.24);
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
    private static final BigDecimal CO2_THRESHOLD_200 = BigDecimal.valueOf(200D);
    private static final BigDecimal CO2_THRESHOLD_150 = BigDecimal.valueOf(150D);
    private static final BigDecimal CO2_THRESHOLD_117 = BigDecimal.valueOf(117D);
    private static final int EMISSION_RATE_200_PLUS = 80;
    private static final int EMISSION_RATE_150_TO_200 = 60;
    private static final int EMISSION_RATE_117_TO_150 = 40;
    private static final int EMISSION_RATE_UP_TO_117 = 5;
    private static final BigDecimal CO2_LOW_EMISSION_RATE = BigDecimal.valueOf(3);
    private static final BigDecimal CO2_MEDIUM_EMISSION_RATE = BigDecimal.valueOf(3.5);
    private static final BigDecimal CO2_HIGH_EMISSION_RATE = BigDecimal.valueOf(4);

    @Override
    public BigDecimal calculateRegistrationTax(CarDetails carDetails) {
        BigDecimal co2Component = calculateCO2Component(carDetails);
        BigDecimal massComponent = calculateMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        BigDecimal ageDiscountFactor = getAgeDiscountFactor(carDetails.getYear());
        BigDecimal discountedCO2AndMass = co2Component.add(massComponent).multiply(ageDiscountFactor);
        return BASE_REGISTRATION_AMOUNT.add(discountedCO2AndMass);
    }

    @Override
    public BigDecimal calculateAnnualTax(CarDetails carDetails) {
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
        BigDecimal co2Emissions = carDetails.getCo2Emissions();
        if (NEDC == carDetails.getCo2Type()) {
            co2Emissions = co2Emissions.multiply(CO2_CONVERSION_FACTOR_NEDC);
        }
        return calculateTaxBasedOnEmissions(co2Emissions);
    }
    
    private BigDecimal calculateTaxBasedOnEmissions(BigDecimal co2Emissions) {
        BigDecimal tax = BigDecimal.ZERO;
        if (is(co2Emissions).greaterThan(CO2_THRESHOLD_200)) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions.subtract(CO2_THRESHOLD_200), EMISSION_RATE_200_PLUS));
            co2Emissions = CO2_THRESHOLD_200;
        }
        if (is(co2Emissions).greaterThan(CO2_THRESHOLD_150)) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions.subtract(CO2_THRESHOLD_150), EMISSION_RATE_150_TO_200));
            co2Emissions = CO2_THRESHOLD_150;
        }
        if (is(co2Emissions).greaterThan(CO2_THRESHOLD_117)) {
            tax = tax.add(calculateTaxForEmissionRange(co2Emissions.subtract(CO2_THRESHOLD_117), EMISSION_RATE_117_TO_150));
            co2Emissions = CO2_THRESHOLD_117;
        }
        tax = tax.add(calculateTaxForEmissionRange(co2Emissions, EMISSION_RATE_UP_TO_117));
        return tax;
    }
    
    private BigDecimal calculateTaxForEmissionRange(BigDecimal emissionDifference, int rate) {
        return emissionDifference.multiply(BigDecimal.valueOf(rate));
    }

    private BigDecimal calculateMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? ELECTRIC_MASS_THRESHOLD : NON_ELECTRIC_MASS_THRESHOLD;
        if (fullMass <= massThreshold) {
            return BigDecimal.ZERO;
        }
        BigDecimal excessMass = BigDecimal.valueOf(fullMass - massThreshold);
        return excessMass.multiply(MASS_TAX_RATE);
    }
    
    private BigDecimal calculateAnnualCO2Component(BigDecimal co2Emissions) {
        if (co2Emissions == null || is(co2Emissions).lessThanOrEqualTo(CO2_THRESHOLD_117)) {
            return BigDecimal.ZERO;
        } else if (is(co2Emissions).lessThanOrEqualTo(CO2_THRESHOLD_150)) {
            return (co2Emissions.subtract(CO2_THRESHOLD_117)).multiply(CO2_LOW_EMISSION_RATE);
        } else if (is(co2Emissions).lessThanOrEqualTo(CO2_THRESHOLD_200)) {
            return (co2Emissions.subtract(CO2_THRESHOLD_150))
                    .multiply(CO2_MEDIUM_EMISSION_RATE).add(calculateAnnualCO2Component(CO2_THRESHOLD_150));
        }
        return (co2Emissions.subtract(CO2_THRESHOLD_200))
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
