package edu.bloomu.km25601.mpchart.sort

/**
 * Enum class to represent the types of sorting
 *
 * @author Karun Mahadevan
 */
enum class SortTypes(private val friendlyName: String) {
    BubbleSort("Bubble Sort"),
    SelectionSort("Selection Sort"),
    InsertionSort("Insertion Sort"),
    QuickSort("Quick Sort"),
    MergeSort("Merge Sort"),;

    override fun toString(): String {
        return friendlyName
    }

}