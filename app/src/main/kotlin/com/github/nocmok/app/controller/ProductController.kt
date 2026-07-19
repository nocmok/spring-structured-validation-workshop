package com.github.nocmok.app.controller

import com.github.nocmok.api.ProductApi
import com.github.nocmok.api.model.CalculateRequest
import com.github.nocmok.api.model.CalculateResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
class ProductController : ProductApi {

    override fun calculate(calculateRequest: CalculateRequest): ResponseEntity<CalculateResponse> {
        return ResponseEntity.ok(CalculateResponse(BigDecimal("100")))
    }
}
