# Spring structured validation workshop

## Overview

In following example request has two problems: `insurant.email` isn't a well-formed email address, and the second entry in `risks` is missing its required `id`.

**Request** - `POST /product/calculate`
```json
{
  "insurant": {
    "name": "Kinyaev Foma",
    "email": "invalid email"
  },
  "risks": [
    {
      "id": "79a66fc2-99b4-4abb-a9ee-eb6a1eed90b0",
      "insuranceType": "LIFE"
    },
    {
      "insuranceType": "PROPERTY"
    }
  ]
}
```

Both violations come back together, in one shot:

**Response** - `400 Bad Request`
```json
{
  "code": "VALIDATION_ERROR",
  "details": [
    {
      "field": "risks[1].id",
      "message": "must not be null"
    },
    {
      "field": "insurant.email",
      "message": "must be a well-formed email address"
    }
  ],
  "message": "invalid request"
}
```

## Requirements

### 1. API design

The API must follow an API-First approach.

### 2. Error response format

Validation errors must be returned as a structured JSON body:

```json
{
  "code": "",
  "message": "",
  "details": [
    {
      "field": "",
      "message": ""
    },
    {
      "field": "",
      "message": ""
    }
  ]
}
```

- `details` is an array of field-level errors, each carrying the offending `field` and a `message` describing the violation.

### 3. Validation behavior

- **Error aggregation** - if a request contains multiple validation errors, all of them must be reported together, one entry per violation in the `details` array.
- **Nested field paths** - the `field` property must contain the complete path to the offending field, including fields nested in objects and objects nested in arrays (e.g. `risks[1].id`).

## Test plan

| ID    | Test case                                              | Preconditions / Input                                                                                                             | Expected result                                                                                                                                                  |
|-------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| TC-01 | Happy path                                             | Request payload is fully valid and satisfies all schema constraints (required fields, types, formats, enums, array sizes, etc.)   | `200 OK`                                                                                                                                                         |
| TC-02 | Empty object payload                                   | Request body is an empty JSON object (`{}`), so every required field is missing                                                   | `400 Bad Request`; `details` array contains one entry per missing required field                                                                                 |
| TC-03 | Missing request body                                   | Request is sent with no body at all (empty payload, no JSON)                                                                      | `400 Bad Request`; `details` array could be empty                                                                                                                |
| TC-04 | Single field-level error: blank string                 | Request payload is otherwise valid except one required string field is set to an empty string where a non-empty value is mandated | `400 Bad Request`; `details` array contains exactly one error entry describing the blank-string violation                                                        |
| TC-05 | Single field-level error: malformed UUID               | Request payload is otherwise valid except one field expected to be a UUID contains a string that is not a valid UUID              | `400 Bad Request`; `details` array contains exactly one error entry describing the invalid UUID format                                                           |
| TC-06 | Single field-level error: invalid enum value           | Request payload is otherwise valid except one enum field contains a value outside the set of allowed enum constants               | `400 Bad Request`; `details` array contains exactly one error entry describing the invalid enum value                                                            |
| TC-07 | Single field-level error: invalid array size           | Request payload is otherwise valid except one array field violates its configured size constraint (min/max number of elements)    | `400 Bad Request`; `details` array contains exactly one error entry describing the array-size violation                                                          |
| TC-08 | Single field-level error: invalid nested array element | Request payload is otherwise valid except one element of an array-of-objects field contains an invalid nested value               | `400 Bad Request`; `details` array contains exactly one error entry whose `field` value encodes the full indexed path to the nested property, e.g. `risks[0].id` |
| TC-09 | Multiple simultaneous errors                           | Request payload violates several validation constraints at once (combination of the error types above)                            | `400 Bad Request`; `details` array contains one entry for every violated constraint, with no errors omitted                                                      |


## How to run

To run application
```shell
SERVER_PORT=8080 ./gradlew app:bootRun
```
Example request
```shell
curl --request POST \
  --url http://localhost:8100/product/calculate \
  --header 'content-type: application/json' \
  --data '{
  "insurant": {
    "name": "Kinyaev Foma",
    "email": "invalid email"
  },
  "risks": [
    {
      "id": "79a66fc2-99b4-4abb-a9ee-eb6a1eed90b0",
      "insuranceType": "LIFE"
    },
    {
      "insuranceType": "PROPERTY"
    }
  ]
}'
```
To run tests
```shell
./gradlew app:test
```
