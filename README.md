# Specmatic Sample: SpringBoot BFF calling Domain API and JMS

* [Specmatic Website](https://specmatic.io)
* [Specmatic Documentation](https://specmatic.io/documentation.html)

This sample project demonstrates how we can practice contract-driven development and contract testing in a SpringBoot (Kotlin) application that depends on an external domain service and JMS.  
Here, Specmatic is used to stub calls to domain API service based on its OpenAPI spec and mock JMS based on its AsyncAPI spec.  
Please contact us at https://specmatic.io if you wish to try it out.

Here is the domain api [contract/open api spec](https://github.com/specmatic/specmatic-order-contracts/blob/main/io/specmatic/examples/store/openapi/api_order_v1.yaml)

Here is the [AsyncAPI spec](https://github.com/specmatic/specmatic-order-contracts/blob/main/io/specmatic/examples/store/asyncapi/jms.yaml) of JMS that defines queues and message schema.

## Definitions
* BFF: Backend for Front End
* Domain API: API managing the domain model
* Specmatic Stub/Mock Server: Create a server that can act as a real service using its OpenAPI or AsyncAPI spec

## Background
A typical web application might look like this. We can use Specmatic to practice contract-driven development and test all the components mentioned below. In this sample project, we look at how to do this for nodejs BFF which is dependent on Domain API Service and JMS demonstrating both OpenAPI and AsyncAPI support in specmatic.

![HTML client talks to client API which talks to backend API](assets/specmatic-order-bff-jms-architecture.gif)

## Tech
1. Spring boot
2. Specmatic
3. Specmatic JMS
4. Karate
5. Docker

## Start BFF Server
This will start the springBoot BFF server
```shell
./gradlew bootRun
```
Access find orders api at http://localhost:8080/findAvailableProducts
_*Note:* Unless domain api service is running on port 9000, above requests will fail. Move to next section for solution!_

### Start BFF Server with Domain API Stub
1. Start domain api mock server
```shell
docker run --rm -p 9000:9000 -v "$(pwd):/usr/src/app" specmatic/specmatic:latest virtualize --port 9000
```
Access find orders api again at http://localhost:8080/findAvailableProducts?type=gadget with result like
```json
[{"id":698,"name":"NUBYR","type":"book","inventory":278}]
```

## Run Tests
This will start the specmatic stub server for domain api using the information in specmatic.json and run the karate tests that expects the domain api at port 9000.
```shell
./gradlew test
```
