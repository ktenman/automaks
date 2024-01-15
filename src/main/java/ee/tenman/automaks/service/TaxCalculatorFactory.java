package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TaxCalculatorFactory {
    public static TaxCalculator getTaxCalculator(CarDetails.CarType carType) {
        return switch (carType) {
            case M1, M1G -> new StandardCarTaxCalculator();
            default -> throw new IllegalArgumentException("Car type not supported yet: " + carType);
        };
    }
}
