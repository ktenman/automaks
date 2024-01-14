package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;

import java.math.BigDecimal;

public class VanTaxCalculator implements TaxCalculator {

    private static final BigDecimal BASE_REGISTRATION_AMOUNT = BigDecimal.valueOf(500);
    private static final Double VAN_CO2_THRESHOLD_LOW = 205.0; // CO2 emissions up to 205 g/km
    private static final Double VAN_CO2_THRESHOLD_MEDIUM = 250.0; // CO2 emissions from 205 to 250 g/km
    private static final Double VAN_CO2_THRESHOLD_HIGH = 300.0; // CO2 emissions from 250 to 300 g/km
    private static final BigDecimal VAN_CO2_LOW_EMISSION_RATE = BigDecimal.valueOf(3.0); // 3 euros per g/km for emissions from 205
    private static final BigDecimal VAN_CO2_MEDIUM_EMISSION_RATE = BigDecimal.valueOf(3.5); // 3.5 euros per g/km for emissions from 251 to 300 g/km
    private static final BigDecimal VAN_CO2_HIGH_EMISSION_RATE = BigDecimal.valueOf(4.0); // 4 euros per g/km for emissions above 300 g/km

    @Override
    public BigDecimal calculateRegistrationTax(CarDetails carDetails) {
        // No mass component calculation for vans, only base amount
        return BASE_REGISTRATION_AMOUNT;
    }

    @Override
    public BigDecimal calculateAnnualTax(CarDetails carDetails) {
        BigDecimal co2Component = calculateVanAnnualCO2Component(carDetails.getCo2Emissions());
        // No mass component calculation for annual tax
        return BASE_ANNUAL_AMOUNT.add(co2Component);
    }

    private BigDecimal calculateVanAnnualCO2Component(Double co2Emissions) {
        if (co2Emissions == null || co2Emissions <= VAN_CO2_THRESHOLD_LOW) {
            return BigDecimal.ZERO;
        } else if (co2Emissions <= VAN_CO2_THRESHOLD_MEDIUM) {
            return BigDecimal.valueOf(co2Emissions - VAN_CO2_THRESHOLD_LOW).multiply(VAN_CO2_LOW_EMISSION_RATE);
        } else if (co2Emissions <= VAN_CO2_THRESHOLD_HIGH) {
            return BigDecimal.valueOf(co2Emissions - VAN_CO2_THRESHOLD_MEDIUM)
                    .multiply(VAN_CO2_MEDIUM_EMISSION_RATE).add(calculateVanAnnualCO2Component(VAN_CO2_THRESHOLD_MEDIUM));
        }
        return BigDecimal.valueOf(co2Emissions - VAN_CO2_THRESHOLD_HIGH)
                .multiply(VAN_CO2_HIGH_EMISSION_RATE).add(calculateVanAnnualCO2Component(VAN_CO2_THRESHOLD_HIGH));
    }
}

