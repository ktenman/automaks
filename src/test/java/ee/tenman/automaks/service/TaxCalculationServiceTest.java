package ee.tenman.automaks.service;

import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.CarDetails.CarType;
import ee.tenman.automaks.dto.TaxResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TaxCalculationServiceTest {

    private final TaxCalculationService service = new TaxCalculationService();

    public static Stream<Arguments> provideCarData() {
        return Stream.of(
                Arguments.of("Porsche Cayenne",
                        CarDetails.builder()
                                .co2Emissions(299D)
                                .fullMass(2860)
                                .carType(CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "16565",
                        "1064"),
                Arguments.of("Audi Q7",
                        CarDetails.builder()
                                .co2Emissions(221D)
                                .fullMass(2850)
                                .carType(CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "10285",
                        "748"),
                Arguments.of("Honda CRV",
                        CarDetails.builder()
                                .co2Emissions(196D)
                                .fullMass(2350)
                                .carType(CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "6365",
                        "450"),
                Arguments.of("VW Passat",
                        CarDetails.builder()
                                .co2Emissions(150D)
                                .fullMass(1990)
                                .carType(CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "2205",
                        "149"),
                Arguments.of("Skoda Octavia",
                        CarDetails.builder()
                                .co2Emissions(117D)
                                .fullMass(1808)
                                .carType(CarType.M1)
                                .year(2019)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "885",
                        "50"),
                Arguments.of("Nissan Leaf",
                        CarDetails.builder()
                                .fullMass(1530)
                                .carType(CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                Arguments.of("Tesla Model 3",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2139)
                                .carType(CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                Arguments.of("Porsche Taycan",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2880)
                                .carType(CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "2220",
                        "242"),
//                Arguments.of("Nissan Berlingo",
//                        CarDetails.builder()
//                                .co2Emissions(150D)
//                                .fullMass(500)
//                                .carType(CarType.N1)
//                                .year(2020)
//                                .electric(false)
//                                .co2Type(CarDetails.CO2Type.WLTP)
//                                .build(),
//                        "1790",
//                        "179"),
//                Arguments.of("Citroen Jumper",
//                        CarDetails.builder()
//                                .co2Emissions(235D)
//                                .fullMass(500)
//                                .carType(CarType.N1)
//                                .year(2022)
//                                .electric(false)
//                                .co2Type(CarDetails.CO2Type.WLTP)
//                                .build(),
//                        "1430",
//                        "143"),
                Arguments.of("VW Tiguan",
                        CarDetails.builder()
                                .co2Emissions(188D)
                                .fullMass(2250)
                                .carType(CarType.M1)
                                .year(2023)
                                .electric(false)
                                .co2Type(CarDetails.CO2Type.WLTP)
                                .build(),
                        "5485",
                        "382"),
                Arguments.of("VW Tiguan without CO2 emissions",
                        CarDetails.builder()
                                .fullMass(2250)
                                .carType(CarType.M1)
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
    void testCalculateTaxes(String model, CarDetails carDetails, BigDecimal expectedRegTax, BigDecimal expectedAnnTax) {
        TaxResponse response = service.calculateTax(carDetails).block();

        assertThat(response.getRegistrationTax()).as("Registration Tax for " + model).isEqualByComparingTo(expectedRegTax);
        assertThat(response.getAnnualTax()).as("Annual Tax for " + model).isEqualByComparingTo(expectedAnnTax);
    }
}

