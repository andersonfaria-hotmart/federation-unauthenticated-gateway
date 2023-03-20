package com.hotmart.crp.federationunauthenticatedgateway

import org.springframework.stereotype.Service

@Service
class FilterService {

  fun filterFields(map: Map<String, Any>, allowed: Map<String, Any>): Map<String, Any> {
    val toFilter = getKeysToFilter(map, allowed)
    val mapToReturn = toFilter.map { tf -> if (allowed[tf] is Map<*, *>) {
        mutableMapOf(tf to filterFields(map[tf]!! as Map<String, Any>, allowed[tf] as Map<String, Any>))
      } else {
        mutableMapOf(tf to map[tf]!!)
      }
    }
    return mapToReturn.flatMap { it.asSequence() }.associate { it.key to it.value }
  }

  private fun getKeysToFilter(map: Map<String, *>, allowed: Map<String, *>) = if (map.keys.all { it.toIntOrNull() != null }) {
      map.values.flatMap {
        when (it) {
          is String -> listOf(it)
          else -> (it as Map<String, *>).keys
        }
      }
    } else {
      map.keys.filter {
        it in allowed || it in allowed.values.flatMap { v ->
          when (v) {
            is String -> listOf(v)
            else -> (v as Map<*, *>).keys
          }
        }
      }
    }
}
