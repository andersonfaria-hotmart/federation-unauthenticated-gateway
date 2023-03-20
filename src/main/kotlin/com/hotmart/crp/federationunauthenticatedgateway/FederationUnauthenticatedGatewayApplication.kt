package com.hotmart.crp.federationunauthenticatedgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Mono

@SpringBootApplication
@EnableConfigurationProperties(HotmartValues::class)
class FederationUnauthenticatedGatewayApplication

fun main(args: Array<String>) {
	runApplication<FederationUnauthenticatedGatewayApplication>(*args)
}

@Configuration
class GatewayConfiguration(private val hotmartValues: HotmartValues, private val filterService: FilterService) {

  @Bean
  fun routes(routeLocatorBuilder: RouteLocatorBuilder): RouteLocator {
    return routeLocatorBuilder.routes {
      route {
        path("/api")
        uri(hotmartValues.crpGatewayUrl)
        filters {
          setPath("/")
          modifyResponseBody(Map::class.java, Map::class.java) { exchange, body ->
            if(exchange.response.statusCode != null && exchange.response.statusCode!!.is2xxSuccessful) {
              val data: Map<String, Any>? = body["data"] as Map<String, Any>?
              val newBody = data?.let {
                filterService.filterFields(it, hotmartValues.allowed)
              }
              Mono.just(mapOf("data" to newBody))
            } else {
              Mono.empty()
            }
          }
        }
      }
    }
  }
}
