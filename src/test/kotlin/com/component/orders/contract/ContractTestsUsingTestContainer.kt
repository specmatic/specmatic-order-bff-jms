package com.component.orders.contract

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIf
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.time.Duration

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnabledIf(value = "isNonCIOrLinux", disabledReason = "Run only on Linux in CI; all platforms allowed locally")
class ContractTestsUsingTestContainer {
    companion object {
        private const val JMS_MOCK_API_SERVER_PORT = 9999
        private val restTemplate: TestRestTemplate = TestRestTemplate()

        @JvmStatic
        fun isNonCIOrLinux(): Boolean =
            System.getenv("CI") != "true" || System.getProperty("os.name").lowercase().contains("linux")

        private fun mockContainer(): GenericContainer<*> = object : GenericContainer<Nothing>(
            "specmatic/enterprise",
        ) {

            override fun start() {
                super.start()
                // wait for container to stabilize
                Thread.sleep(20000)
            }

            override fun stop() {
                dumpReports()
                super.stop()
            }


            private fun dumpReports() {
                println("Dumping kafka mock reports..")
                val response: ResponseEntity<String> =
                    restTemplate.exchange(
                        URI("http://localhost:$JMS_MOCK_API_SERVER_PORT/stop"),
                        HttpMethod.POST,
                        HttpEntity(""),
                        String::class.java,
                    )
                if (response.statusCode == HttpStatusCode.valueOf(200)) {
                    println("Reports dumped successfully!")
                } else {
                    println("Error occurred while dumping the reports")
                }
            }
        }

        @Container
        private val mockContainer: GenericContainer<*> =
            mockContainer()
                .withCommand("mock")
                .withFileSystemBind("./src", "/usr/src/app/src", BindMode.READ_ONLY)
                .withFileSystemBind("./specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
                .withFileSystemBind(
                    "./build/reports/specmatic",
                    "/usr/src/app/build/reports/specmatic",
                    BindMode.READ_WRITE
                )
                .withNetworkMode("host")
                .withLogConsumer { print(it.utf8String) }

        private val testContainer: GenericContainer<*> =
            GenericContainer("specmatic/enterprise")
                .withCommand("test")
                .withFileSystemBind("./src", "/usr/src/app/src", BindMode.READ_ONLY)
                .withFileSystemBind("./specmatic.yaml", "/usr/src/app/specmatic.yaml", BindMode.READ_ONLY)
                .withFileSystemBind(
                    "./build/reports/specmatic",
                    "/usr/src/app/build/reports/specmatic",
                    BindMode.READ_WRITE
                )
                .withNetworkMode("host")
                .waitingFor(
                    Wait.forLogMessage(".*Tests run:.*", 1)
                        .withStartupTimeout(Duration.ofMinutes(2))
                )
                .withLogConsumer { print(it.utf8String) }
    }

    @Test
    fun specmaticContractTest() {
        testContainer.start()
        val hasSucceeded = testContainer.logs.contains("Failures: 0")
        assertThat(hasSucceeded).isTrue()
    }
}
