package com.hotmart.crp.federationunauthenticatedgateway

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [FederationUnauthenticatedGatewayApplicationTests.Companion.PropertyOverrideContextInitializer::class])
@AutoConfigureMockMvc
class FederationUnauthenticatedGatewayApplicationTests(@LocalServerPort private val port: Int) {

  private lateinit var client: WebTestClient

  companion object {
    var server: WireMockServer = WireMockServer(8080)

    class PropertyOverrideContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
      override fun initialize(applicationContext: ConfigurableApplicationContext) {
          TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext, "hotmart.crpGatewayUrl=http://localhost:8080"
          )
      }
    }

    @JvmStatic
    @BeforeAll
    fun setup() {
      server.start()
    }

    @JvmStatic
    @AfterAll
    fun teardown() {
      server.shutdownServer()
    }
  }

  @BeforeEach
  fun url() {
    MockitoAnnotations.openMocks(this)
    client = WebTestClient.bindToServer().baseUrl("http://localhost:$port").build()
  }

  @Test
  fun `api gateway should pass the generic error without modifying it`() {
    WireMock.stubFor((WireMock.post("/")).willReturn(WireMock.serverError()))
    client.post().uri("/api").header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).body(BodyInserters.fromValue(Mockito.anyString()))
      .exchange()
      .expectStatus().is5xxServerError
  }


  @Test
  fun `full response and gateway should allow just what is configured in application yml`() {
    @Language("JSON") val response = """
        {
            "data": {
                "purchaseByTransaction": {
                    "id": 7735898,
                    "buyer": {
                        "email": "6e0fe@test.com"
                    },
                    "product": {
                        "clubMembershipLink": "https://002comodeixardeserotario.app-club.buildstaging.com",
                        "productDeliveryMethod": "POSTBACK"
                    }
                }
            }
        }
        """


    @Language("JSON") val expectedResponse = """
      {
          "data": {
              "purchaseByTransaction": {
                  "id": 7735898,
                  "buyer": {
                      "email": "6e0fe@test.com"
                  }
              }
          }
      }
      """
    WireMock.stubFor((WireMock.post("/")).willReturn(WireMock.okJson(response)))

    client.post().uri("/api").header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).body(BodyInserters.fromValue(Mockito.anyString()))
      .exchange()
      .expectStatus().is2xxSuccessful
      .expectBody()
      .json(expectedResponse)
  }

}
