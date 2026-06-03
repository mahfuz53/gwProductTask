package com.gwproductsusa.gwtasks.data.remote.request

/**
 * Odoo search domain format for execute_kw search_read:
 *
 * ```
 * "search_read",
 * [
 *   [
 *     ["id", "=", 2]
 *   ]
 * ]
 * ```
 *
 * Structure: outer list = domain, middle = condition group, inner = triplet [field, operator, value].
 */
typealias OdooSearchDomain = List<List<List<Any?>>>

object OdooDomain {

    fun empty(): OdooSearchDomain = emptyList()

    fun eq(field: String, value: Any): OdooSearchDomain =
        listOf(listOf(listOf(field, "=", value)))
}
