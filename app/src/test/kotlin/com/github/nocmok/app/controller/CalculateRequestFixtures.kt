package com.github.nocmok.app.controller

import java.util.UUID

fun validInsurantJson(
    name: String = "John Doe",
    email: String = "john.doe@example.com",
) = """{"name":"$name","email":"$email"}"""

fun validRiskJson(
    id: String = UUID.randomUUID().toString(),
    insuranceType: String = "LIFE",
) = """{"id":"$id","insuranceType":"$insuranceType"}"""

fun requestJson(risksJson: String = "[${validRiskJson()}]", insurantJson: String = validInsurantJson()) =
    """{"risks":$risksJson,"insurant":$insurantJson}"""
