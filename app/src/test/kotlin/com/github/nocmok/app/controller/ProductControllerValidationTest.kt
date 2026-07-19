package com.github.nocmok.app.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
class ProductControllerValidationTest {

    @Autowired
    private lateinit var context: WebApplicationContext

    private lateinit var client: RestTestClient

    @BeforeEach
    fun setUp() {
        client = RestTestClient
            .bindToApplicationContext(context)
            .build()
    }

    @Test
    @DisplayName("TC-01: fully valid payload returns 200 OK")
    fun `fully valid payload returns 200`() {
        val body = requestJson()

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    @DisplayName("TC-02: empty object payload returns 400 with one detail per missing required field")
    fun `empty object payload returns 400 with details for every missing required field`() {
        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body("{}")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(2)
            .jsonPath("$.details[*].field").value { fields: List<*> ->
                assertThat(fields).containsExactlyInAnyOrder("risks", "insurant")
            }
    }

    @Test
    @DisplayName("TC-03: missing request body returns 400")
    fun `missing request body returns 400`() {
        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
    }

    @Test
    @DisplayName("TC-04: blank required string field returns exactly one detail entry")
    fun `blank insurant name returns exactly one detail entry`() {
        val body = requestJson(insurantJson = validInsurantJson(name = "   "))

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(1)
            .jsonPath("$.details[0].field").isEqualTo("insurant.name")
    }

    @Test
    @DisplayName("TC-05: malformed UUID returns exactly one detail entry")
    fun `malformed risk id returns exactly one detail entry`() {
        val body = requestJson(risksJson = "[${validRiskJson(id = "not-a-uuid")}]")

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(1)
            .jsonPath("$.details[0].field").isEqualTo("risks[0].id")
    }

    @Test
    @DisplayName("TC-06: invalid enum value returns exactly one detail entry")
    fun `invalid insurance type returns exactly one detail entry`() {
        val body = requestJson(risksJson = "[${validRiskJson(insuranceType = "NOT_A_TYPE")}]")

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(1)
            .jsonPath("$.details[0].field").isEqualTo("risks[0].insuranceType")
    }

    @Test
    @DisplayName("TC-07: array size violation returns exactly one detail entry")
    fun `empty risks array returns exactly one detail entry`() {
        val body = requestJson(risksJson = "[]")

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(1)
            .jsonPath("$.details[0].field").isEqualTo("risks")
    }

    @Test
    @DisplayName("TC-08: invalid nested array element reports full indexed path")
    fun `invalid element inside risks array reports indexed field path`() {
        val body = requestJson(risksJson = "[${validRiskJson()},${validRiskJson(id = "not-a-uuid")}]")

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(1)
            .jsonPath("$.details[0].field").isEqualTo("risks[1].id")
    }

    @Test
    @DisplayName("TC-09: multiple violations are all reported with no omissions")
    fun `multiple simultaneous violations are all reported`() {
        val body = requestJson(risksJson = "[]", insurantJson = validInsurantJson(name = " ", email = "not-an-email"))

        client.post().uri("/product/calculate")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.code").isEqualTo("VALIDATION_ERROR")
            .jsonPath("$.details.length()").isEqualTo(3)
            .jsonPath("$.details[*].field").value { fields: List<*> ->
                assertThat(fields).containsExactlyInAnyOrder("risks", "insurant.name", "insurant.email")
            }
    }
}
