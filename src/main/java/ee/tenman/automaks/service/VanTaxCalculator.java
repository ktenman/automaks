package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;

import java.math.BigDecimal;

public class VanTaxCalculator implements TaxCalculator {

    private static final BigDecimal BASE_REGISTRATION_AMOUNT = BigDecimal.valueOf(500);

    @Override
    public BigDecimal calculateRegistrationTax(CarDetails carDetails) {
        BigDecimal massComponent = calculateVanMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        return BASE_REGISTRATION_AMOUNT.add(massComponent);
    }

    @Override
    public BigDecimal calculateAnnualTax(CarDetails carDetails) {
        BigDecimal co2Component = calculateVanAnnualCO2Component(carDetails.getCo2Emissions());
        BigDecimal massComponent = calculateVanAnnualMassComponent(carDetails.getFullMass(), carDetails.isElectric());
        return BASE_ANNUAL_AMOUNT.add(co2Component).add(massComponent);
    }

    private BigDecimal calculateVanMassComponent(int fullMass, boolean isElectric) {
        int massThreshold = isElectric ? ELECTRIC_MASS_THRESHOLD : NON_ELECTRIC_MASS_THRESHOLD;
        if (fullMass <= massThreshold) {
            return BigDecimal.ZERO;
        }
        BigDecimal excessMass = BigDecimal.valueOf(fullMass - massThreshold);
        return excessMass.multiply(MASS_TAX_RATE);
    }


    private BigDecimal calculateVanAnnualCO2Component(Double co2Emissions) {
        // Implementation for CO2 component calculation for vans
        // Similar to the StandardCarTaxCalculator but might have different thresholds and rates
        return null;
    }

    private BigDecimal calculateVanAnnualMassComponent(int fullMass, boolean isElectric) {
        // Implementation for annual mass component calculation for vans
        // Similar to the StandardCarTaxCalculator but might have different thresholds and rates
        return null;
    }

}
