package com.hotmart.crp.federationunauthenticatedgateway

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hotmart")
data class HotmartValues(val crpGatewayUrl: String, val allowed: Map<String, Map<String, Any>>)
