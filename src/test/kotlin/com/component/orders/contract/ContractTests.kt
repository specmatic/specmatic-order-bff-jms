package com.component.orders.contract

import io.specmatic.enterprise.SpecmaticContractTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ContractTests : SpecmaticContractTest {
    @BeforeAll
    fun awaitHealth() {
        val client = TestRestTemplate()
        await().atMost(java.time.Duration.ofSeconds(30)).until {
            client.getForEntity("http://localhost:8080/actuator/health", String::class.java).statusCode == HttpStatus.OK
        }
    }
}
