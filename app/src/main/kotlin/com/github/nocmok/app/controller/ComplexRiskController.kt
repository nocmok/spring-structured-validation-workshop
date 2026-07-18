package com.github.nocmok.app.controller

import com.github.nocmok.api.ComplexRiskApi
import com.github.nocmok.api.model.CreateComplexRiskRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class ComplexRiskController : ComplexRiskApi {

    override fun createComplexRisk(createComplexRiskRequest: CreateComplexRiskRequest): ResponseEntity<Unit> {
        return ResponseEntity(HttpStatus.OK)
    }
}
