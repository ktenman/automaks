package ee.tenman.automaks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Set;

import static ee.tenman.automaks.dto.CarDetails.CarType.N1;
import static ee.tenman.automaks.dto.CarDetails.CarType.N1G;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@CarDetails.ValidCarDetails
public class CarDetails {

    private static final Set<CarType> VAN_TYPES = Set.of(N1, N1G);

    @Schema(description = "CO2 emissions of the car", example = "188.0", nullable = true)
    private Double co2Emissions;

    @Schema(required = true, description = "The full mass of the car in kilograms", example = "2250")
    private Integer fullMass;

    @Schema(required = true, description = "The year of manufacture of the car", example = "2023")
    @NotNull
    private Integer year;

    @Schema(description = "Engine capacity of the car in cubic centimeters", nullable = true)
    private Integer engineCapacity;

    @Schema(description = "Engine power of the car in horsepower", nullable = true)
    private Integer enginePower;

    @Schema(required = true, description = "The type of car, as defined by the CarType enum", example = "M1")
    @NotNull
    private CarType carType;

    @Schema(description = "Whether the car is electric or not", example = "false")
    private boolean electric;

    @Schema(description = "The type of CO2 measurement standard, as defined by the CO2Type enum", nullable = true, example = "WLTP")
    private CO2Type co2Type;

    public enum CarType {
        @Schema(description = "Passenger cars with up to eight seats, not including the driver's seat")
        M1,
        @Schema(description = "Off-road passenger cars with up to eight seats, not including the driver's seat")
        M1G,
        @Schema(description = "Vehicles used for the carriage of goods with a maximum weight not exceeding 3.5 tonnes")
        N1,
        @Schema(description = "Off-road vehicles used for the carriage of goods with a maximum weight not exceeding 3.5 tonnes")
        N1G,
        @Schema(description = "Two-wheel motorcycles with or without a sidecar, with engine power not exceeding 15 kW")
        L3e,
        @Schema(description = "Motorcycles with four wheels, with an unladen mass not exceeding 350 kg")
        L4e,
        @Schema(description = "Motor tricycles with a power output not exceeding 15 kW")
        L5e,
        @Schema(description = "Light quadricycles with an unladen mass not exceeding 350 kg")
        L6e,
        @Schema(description = "Quadricycles other than light quadricycles")
        L7e,
        @Schema(description = "Special purpose M category vehicles other than M1, designed for the carriage of passengers")
        MS2,
        @Schema(description = "Trailers designed for the carriage of goods with a maximum weight not exceeding 0.75 tonnes")
        T1b,
        @Schema(description = "Tractors with a maximum design speed not exceeding 40 km/h")
        T3,
        @Schema(description = "Commercial trailers with a maximum weight exceeding 3.5 tonnes")
        T5
    }

    public enum CO2Type {
        @Schema(description = "Worldwide Harmonized Light Vehicles Test Procedure")
        WLTP,
        @Schema(description = "New European Driving Cycle")
        NEDC
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

}
