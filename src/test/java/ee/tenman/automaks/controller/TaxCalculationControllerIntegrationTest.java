package ee.tenman.automaks.controller;

import ee.tenman.automaks.config.GlobalExceptionHandler;
import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaxCalculationControllerIntegrationTest {

    @Resource
    WebTestClient webTestClient;

    public static Stream<Arguments> provideCarData() {
        return Stream.of(
                Arguments.of("Porsche Cayenne",
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
                Arguments.of("Audi Q7",
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
                Arguments.of("Honda CRV",
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
                Arguments.of("VW Passat",
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
                Arguments.of("Skoda Octavia",
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
                Arguments.of("Nissan Leaf",
                        CarDetails.builder()
                                .fullMass(1530)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                Arguments.of("Tesla Model 3",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2139)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "300",
                        "50"),
                Arguments.of("Porsche Taycan",
                        CarDetails.builder()
                                .co2Emissions(0D)
                                .fullMass(2880)
                                .carType(CarDetails.CarType.M1)
                                .year(2021)
                                .electric(true)
                                .build(),
                        "2220",
                        "242"),
                Arguments.of("VW Tiguan",
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
                Arguments.of("VW Tiguan without CO2 emissions",
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
    void testCalculateTaxEndpoint(
            String model,
            CarDetails carDetails,
            BigDecimal expectedRegistrationTax,
            BigDecimal expectedAnnualTax
    ) {
        webTestClient.post().uri("/tax/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carDetails)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaxResponse.class)
                .consumeWith(response -> {
                    TaxResponse calculatedTax = response.getResponseBody();
                    assertThat(calculatedTax.registrationTax()).as("Registration Tax for " + model).isEqualByComparingTo(expectedRegistrationTax);
                    assertThat(calculatedTax.annualTax()).as("Annual Tax for " + model).isEqualByComparingTo(expectedAnnualTax);
                });
    }

    @Test
    void testCalculateTaxEndpoint_whenBadRequestWhenEngineCapacityMissing() {
        CarDetails carDetails = CarDetails.builder()
                .carType(CarDetails.CarType.M1)
                .fullMass(2000)
                .year(2019)
                .enginePower(150)
                .build();
        webTestClient.post().uri("/tax/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carDetails)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(GlobalExceptionHandler.ApiError.class)
                .consumeWith(response -> {
                    GlobalExceptionHandler.ApiError apiError = response.getResponseBody();
                    assertThat(apiError.getStatus().value()).as("Status code").isEqualTo(400);
                    assertThat(apiError.getMessage()).as("Message").isEqualTo("Validation error");
                    assertThat(apiError.getDebugMessage()).as("Debug message").isEqualTo("One or more fields have an error");
                    assertThat(apiError.getValidationErrors()).as("Validation errors")
                            .containsOnly(Map.entry("engineCapacity", "Missing engineCapacity or/and enginePower for non-electric vehicle.")
                            );
                });
    }

    @Test
    void testCalculateTaxEndpoint_whenBadRequestAndCO2TypeMissing() {
        CarDetails carDetails = CarDetails.builder()
                .carType(CarDetails.CarType.M1)
                .fullMass(2000)
                .year(2019)
                .co2Emissions(150D)
                .build();
        webTestClient.post().uri("/tax/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carDetails)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(GlobalExceptionHandler.ApiError.class)
                .consumeWith(response -> {
                    GlobalExceptionHandler.ApiError apiError = response.getResponseBody();
                    assertThat(apiError.getStatus().value()).as("Status code").isEqualTo(400);
                    assertThat(apiError.getMessage()).as("Message").isEqualTo("Validation error");
                    assertThat(apiError.getDebugMessage()).as("Debug message").isEqualTo("One or more fields have an error");
                    assertThat(apiError.getValidationErrors()).as("Validation errors")
                            .containsOnly(Map.entry("co2Type", "Missing CO2Type for vehicle with co2Emissions.")
                            );
                });
    }
    
    @Test
    void testCalculateTaxEndpoint_whenCo2EmissionsIsNaN() {
        CarDetails carDetails = CarDetails.builder()
                .co2Emissions(Double.NaN)
                .fullMass(2250)
                .carType(CarDetails.CarType.M1)
                .engineCapacity(1995)
                .enginePower(150)
                .year(2023)
                .electric(false)
                .co2Type(CarDetails.CO2Type.WLTP)
                .build();
        
        webTestClient.post().uri("/tax/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(carDetails)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaxResponse.class)
                .consumeWith(response -> {
                    TaxResponse calculatedTax = response.getResponseBody();
                    assertThat(calculatedTax.registrationTax()).isNotNull().isEqualByComparingTo(new BigDecimal("2599.75"));
                    assertThat(calculatedTax.annualTax()).isNotNull().isEqualByComparingTo(new BigDecimal("150"));
                });
    }
    
}
