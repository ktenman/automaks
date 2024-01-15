package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StandardCarTaxCalculatorTest {

    private final StandardCarTaxCalculator calculator = new StandardCarTaxCalculator();

    public static Stream<CarTaxTestData> provideCarData() {
        return Stream.of(
                new CarTaxTestData("Porsche Cayenne",
                        CarDetails.builder()
                                .co2Emissions(299D)
                                .fullMass(2860)
                                .carType(CarDetails.CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "16565",
                        "1064"),
                new CarTaxTestData("Audi Q7",
                        CarDetails.builder()
                                .co2Emissions(221D)
                                .fullMass(2850)
                                .carType(CarDetails.CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "10285",
                        "748"),
                new CarTaxTestData("Honda CRV",
                        CarDetails.builder()
                                .co2Emissions(196D)
                                .fullMass(2350)
                                .carType(CarDetails.CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "6365",
                        "450"),
                new CarTaxTestData("VW Passat",
                        CarDetails.builder()
                                .co2Emissions(150D)
                                .fullMass(1990)
                                .carType(CarDetails.CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "2205",
                        "149"),
                new CarTaxTestData("Skoda Octavia",
                        CarDetails.builder()
                                .co2Emissions(117D)
                                .fullMass(1808)
                                .carType(CarDetails.CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "885",
                        "50"),
                new CarTaxTestData("Nissan Leaf",
                        CarDetails.builder()
                                .fullMass(1530)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                new CarTaxTestData("Tesla Model 3",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2139)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                new CarTaxTestData("Porsche Taycan",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2880)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "2220",
                        "242"),
                new CarTaxTestData("VW Tiguan",
                        CarDetails.builder()
                                .co2Emissions(188D)
                                .fullMass(2250)
                                .carType(CarDetails.CarType.M1)
                                .year(2023)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "5485",
                        "382"),
                new CarTaxTestData("VW Tiguan without CO2 emissions",
                        CarDetails.builder()
                                .fullMass(2250)
                                .carType(CarDetails.CarType.M1)
                                .year(2023)
                                .electric(false)
                                .engineCapacity(1995)
                                .enginePower(150)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "2599.75",
                        "150")
        );
    }

    @ParameterizedTest
    @MethodSource("provideCarData")
    void calculateRegistrationTax(CarTaxTestData carTaxTestData) {
        BigDecimal registrationTax = calculator.calculateRegistrationTax(carTaxTestData.carDetails);

        assertThat(registrationTax).as("Registration Tax for " + carTaxTestData.model)
                .isEqualByComparingTo(new BigDecimal(carTaxTestData.expectedRegistrationTax));
    }

    @ParameterizedTest
    @MethodSource("provideCarData")
    void calculateAnnualTax(CarTaxTestData carTaxTestData) {
        BigDecimal annualTax = calculator.calculateAnnualTax(carTaxTestData.carDetails);

        assertThat(annualTax).as("Annual Tax for " + carTaxTestData.model)
                .isEqualByComparingTo(new BigDecimal(carTaxTestData.expectedAnnualTax));
    }

    public record CarTaxTestData(
            String model,
            CarDetails carDetails,
            String expectedRegistrationTax,
            String expectedAnnualTax
    ) {
    }

}
