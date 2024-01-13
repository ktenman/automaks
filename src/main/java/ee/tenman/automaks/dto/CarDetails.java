package ee.tenman.automaks.dto;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@CarDetails.ValidCarDetails
public class CarDetails {

    private Double co2Emissions;
    @NotNull
    private Integer fullMass;
    @NotNull
    private Integer year;
    private Integer engineCapacity;
    private Integer enginePower;
    @NotNull
    private CarType carType;
    private boolean electric;
    private CO2Type co2Type;

    public enum CarType {
        M1,
        M1G,
        N1,
        N1G,
        L3e,
        L4e,
        L5e,
        L6e,
        L7e,
        MS2,
        T1b,
        T3,
        T5
    }

    public enum CO2Type {
        WLTP, NEDC
    }

    @Documented
    @Constraint(validatedBy = CarDetailsValidator.class)
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ValidCarDetails {
        String message() default "Invalid car details";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    @Slf4j
    public static class CarDetailsValidator implements ConstraintValidator<ValidCarDetails, CarDetails> {

        @Override
        public boolean isValid(CarDetails carDetails, ConstraintValidatorContext context) {
            boolean valid = true;
            if (carDetails.getCo2Emissions() == null && !carDetails.isElectric()) {
                if (carDetails.getEngineCapacity() == null || carDetails.getEnginePower() == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Missing engineCapacity or/and enginePower for non-electric vehicle.")
                            .addPropertyNode("engineCapacity")
                            .addConstraintViolation();
                    valid = false;
                }
            } else if (carDetails.getCo2Emissions() != null && carDetails.getCo2Type() == null && !carDetails.isElectric()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Missing CO2Type for vehicle with co2Emissions.")
                        .addPropertyNode("co2Type")
                        .addConstraintViolation();
                valid = false;
            }

            return valid;
        }
    }

}
