package ee.tenman.automaks.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
public class TaxResponse {
    private BigDecimal registrationTax;
    private BigDecimal annualTax;
    private double durationInSeconds;
}
