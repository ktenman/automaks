package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;

import java.math.BigDecimal;

public class VanTaxCalculator implements TaxCalculator {

    @Override
    public BigDecimal calculateRegistrationTax(CarDetails carDetails) {
        return null;
    }

    @Override
    public BigDecimal calculateAnnualTax(CarDetails carDetails) {
        return null;
    }
}

