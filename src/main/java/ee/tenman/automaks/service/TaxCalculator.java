package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;

import java.math.BigDecimal;

public interface TaxCalculator {

    BigDecimal BASE_ANNUAL_AMOUNT = BigDecimal.valueOf(50);
    int ELECTRIC_MASS_THRESHOLD = 2400;
    int NON_ELECTRIC_MASS_THRESHOLD = 2000;
    BigDecimal MASS_TAX_RATE = BigDecimal.valueOf(4);

    BigDecimal calculateRegistrationTax(CarDetails carDetails);

    BigDecimal calculateAnnualTax(CarDetails carDetails);
}

