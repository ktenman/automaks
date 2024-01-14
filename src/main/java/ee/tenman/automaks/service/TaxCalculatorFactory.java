package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class TaxCalculatorFactory {
    public static TaxCalculator getTaxCalculator(CarDetails.CarType carType) {
        return switch (carType) {
            case N1, N1G -> new VanTaxCalculator();
            default -> new StandardCarTaxCalculator();
        };
    }
}
