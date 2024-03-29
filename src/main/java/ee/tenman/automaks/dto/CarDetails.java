package ee.tenman.automaks.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@ValidCarDetails
public class CarDetails {

    @Schema(description = "CO2 emissions of the car", example = "188.0", nullable = true)
    private BigDecimal co2Emissions;

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

}
