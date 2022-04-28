package edu.bloomu.km25601.mpchart.search

/**
 * Enum class to represent the searching types
 *
 * @author Karun Mahadevan
 */
enum class SearchTypes(private val friendlyName: String) {
    Linear("Linear Search"),
    OptimLinear("Optimized Linear Search"),
    Binary("Binary Search"),
    Ternary("Ternary Search"),
    Jump("Jump Search"),
    Expo("Exponential Search");

    override fun toString(): String {
        return friendlyName
    }

}