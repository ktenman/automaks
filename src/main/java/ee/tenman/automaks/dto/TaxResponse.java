package ee.tenman.automaks.dto;

import java.math.BigDecimal;

public record TaxResponse(BigDecimal registrationTax, BigDecimal annualTax) {
}

