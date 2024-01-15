package ee.tenman.automaks.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

import static ee.tenman.automaks.dto.CarDetails.CarType.N1;
import static ee.tenman.automaks.dto.CarDetails.CarType.N1G;

@Slf4j
public class CarDetailsValidator implements ConstraintValidator<ValidCarDetails, CarDetails> {

    private static final Set<CarDetails.CarType> VAN_TYPES = Set.of(N1, N1G);

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

        if (carDetails.getFullMass() == null && !VAN_TYPES.contains(carDetails.getCarType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid car type for missing fullMass. Car type must be one of " + VAN_TYPES)
                    .addPropertyNode("fullMass")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
