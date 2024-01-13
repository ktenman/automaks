package ee.tenman.automaks.controller;

import ee.tenman.automaks.config.GlobalExceptionHandler;
import ee.tenman.automaks.dto.CarDetails;
import ee.tenman.automaks.dto.TaxResponse;
import ee.tenman.automaks.service.TaxCalculationServiceTest;
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

    private static Stream<Arguments> provideCarData() {
        return TaxCalculationServiceTest.provideCarData();
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
                    assertThat(calculatedTax.getRegistrationTax()).as("Registration Tax for " + model).isEqualByComparingTo(expectedRegistrationTax);
                    assertThat(calculatedTax.getAnnualTax()).as("Annual Tax for " + model).isEqualByComparingTo(expectedAnnualTax);
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
}
