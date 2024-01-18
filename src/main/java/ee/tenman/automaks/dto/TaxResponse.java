package ee.tenman.automaks.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TaxResponse(BigDecimal registrationTax, BigDecimal annualTax) {
	
	public TaxResponse(BigDecimal registrationTax, BigDecimal annualTax) {
		this.registrationTax = (registrationTax != null)
				? registrationTax.setScale(2, RoundingMode.HALF_UP)
				: null;
		this.annualTax = (annualTax != null)
				? annualTax.setScale(2, RoundingMode.HALF_UP)
				: null;
	}
}
