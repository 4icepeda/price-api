package com.inditex.pricing.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * System tests for the Price REST API using @SpringBootTest + MockMvc.
 * These tests exercise the full application stack (controller -> use case -> persistence)
 * against the H2 database initialized by Flyway with the seed data.
 *
 * Covers the 5 required test scenarios from the specification.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerSystemTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String API_URL = "/api/prices";

    // ==========================================
    // 5 Required Test Scenarios
    // ==========================================

    @Test
    @DisplayName("Test 1: Request at 10:00 on June 14 -> price_list=1, price=35.50")
    void test1_requestAt10OnJune14_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    @DisplayName("Test 2: Request at 16:00 on June 14 -> price_list=2, price=25.45")
    void test2_requestAt16OnJune14_shouldReturnPriceList2() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T16:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(2))
                .andExpect(jsonPath("$.price").value(25.45));
    }

    @Test
    @DisplayName("Test 3: Request at 21:00 on June 14 -> price_list=1, price=35.50")
    void test3_requestAt21OnJune14_shouldReturnPriceList1() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T21:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.price").value(35.50));
    }

    @Test
    @DisplayName("Test 4: Request at 10:00 on June 15 -> price_list=3, price=30.50")
    void test4_requestAt10OnJune15_shouldReturnPriceList3() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-15T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(3))
                .andExpect(jsonPath("$.price").value(30.50));
    }

    @Test
    @DisplayName("Test 5: Request at 21:00 on June 16 -> price_list=4, price=38.95")
    void test5_requestAt21OnJune16_shouldReturnPriceList4() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-16T21:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(4))
                .andExpect(jsonPath("$.price").value(38.95));
    }

    // ==========================================
    // Additional edge case & error handling tests
    // ==========================================

    @Test
    @DisplayName("Should return 404 when no price applies for the given parameters")
    void shouldReturn404WhenNoPriceApplies() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2019-01-01T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when applicationDate is missing")
    void shouldReturn400WhenApplicationDateIsMissing() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when applicationDate has invalid format")
    void shouldReturn400WhenApplicationDateHasInvalidFormat() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "not-a-date")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "99999")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return correct date range in response for Test 2")
    void shouldReturnCorrectDateRangeInResponse() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T16:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startDate").value("2020-06-14T15:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-06-14T18:30:00"))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("Should return 400 when productId is zero")
    void shouldReturn400WhenProductIdIsZero() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "0")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when productId is negative")
    void shouldReturn400WhenProductIdIsNegative() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "-1")
                        .param("brandId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when brandId is zero")
    void shouldReturn400WhenBrandIdIsZero() throws Exception {
        mockMvc.perform(get(API_URL)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
