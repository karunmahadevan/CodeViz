@file:Suppress("BlockingMethodInNonBlockingContext")
package edu.bloomu.km25601.mpchart.sort

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.google.android.material.slider.Slider
import edu.bloomu.km25601.mpchart.R
import edu.bloomu.km25601.mpchart.Utils
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.properties.Delegates

@DelicateCoroutinesApi
class Sort : Fragment(), View.OnClickListener {
    //Views
    private lateinit var sortMethods: Spinner
    private lateinit var sortChart: Button
    private lateinit var homeButton: Button
    private lateinit var slider: Slider
    private lateinit var sliderText: TextView
    private lateinit var sortInfo: TextView
    private lateinit var help: Button
    private lateinit var titleText: TextView

    //Objects needed for MPAndroid bar chart
    private lateinit var chart: BarChart
    private lateinit var points: IntArray
    private lateinit var data: BarData
    private lateinit var barDataSet: BarDataSet
    private lateinit var barColorArray: ArrayList<Int>
    private var entries: ArrayList<BarEntry> = ArrayList()

    //Placeholders to be instantiated on OnCreateView
    private var width = 0
    private var height = 0
    private var lb = 0
    private var purp = 0
    private var fin = 0
    private var sleep = 0
    private var size = 0
    
    //Coroutine Scopes
    private val defaultScope = Dispatchers.Default
    private val mainScope = Dispatchers.Main

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View =  inflater.inflate(R.layout.fragment_sort, container, false)

        width = resources.displayMetrics.widthPixels
        height = resources.displayMetrics.heightPixels

        //Instantiating views
        help = v.findViewById(R.id.testSwitch)
        chart = v.findViewById(R.id.sortGraph)
        sortChart = v.findViewById(R.id.sortChart)
        homeButton = v.findViewById(R.id.homeButton)
        sliderText = v.findViewById(R.id.sliderText)
        slider = v.findViewById(R.id.sortSlider)
        sortInfo = v.findViewById(R.id.sortInfo)
        titleText = v.findViewById(R.id.titleTextSort)
        sortMethods = v.findViewById(R.id.sortMethods)

        //Assigning colors that are used throughout the class
        lb = ContextCompat.getColor(requireContext(), R.color.bgGradEnd)
        purp = ContextCompat.getColor(requireContext(), R.color.bgGradStart)
        fin = ContextCompat.getColor(requireContext(), R.color.second)


        //chart properties
        Utils.chartProperties(chart)
        Utils.setH(chart, (height * .75).toInt())

        //sliderText text properties
        Utils.setW(sliderText, (width * .4).toInt())

        //sortMethods
        sortMethods.adapter = ArrayAdapter(requireContext(),
            R.layout.spinnerview,
            SortTypes.values())

        sortMethods.layoutParams.width = (resources.displayMetrics.widthPixels * .3)
            .toInt()
        sortMethods.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                sortInfo.text = getSpinnerText()
                Utils.setGradText(getSpinnerTitle(), titleText)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                sortInfo.text = getSpinnerText()
                Utils.setGradText(getSpinnerTitle(), titleText)
            }
        }

        //slider properties
        slider.valueFrom = 50f
        slider.valueTo = 200f
        slider.value = 50f
        slider.stepSize = 1f
        slider.addOnChangeListener { _, _, _ ->
            initChart()
        }

        //titleText properties
        Utils.setGradText(getSpinnerTitle(), titleText)

        //sortChart properties
        Utils.setW(sortChart, (width * .3).toInt())

        //Setting onClick listeners
        sortChart.setOnClickListener(this)
        homeButton.setOnClickListener(this)
        help.setOnClickListener(this)

        //Initialize chart
        initChart()

        return v
    }

    /**
     * Overridden onClick listener that, depending on the view, performs the
     * correct respective action
     *
     * @param view is the view is being assigned an onClick listener
     */
    override fun onClick(view: View) {
        when (view) {
            help -> {
                Utils.replaceFrag(
                    requireActivity().supportFragmentManager.beginTransaction
                        (), SortHelp())
            }

            homeButton ->  {
                mainScope.cancel()
                Utils.returnToHome(this)
            }
            sortChart -> {
                sortChart.isClickable = false
                when (sortMethods.selectedItemPosition) {
                    0 -> bubbleSort()
                    1 -> selectionSort()
                    2 -> insertionSort()
                    3 -> doQuickSort()
                    4 -> doMergeSorting()
                }
                Utils.setGradText(getSpinnerTitle(), titleText)
            }
        }
    }

    /**
     * Function that sets the starting data for the chart to sort
     *
     * @param size is the size of the data (# of elements needed)
     */
    private fun setStartingData(size: Int) {
        val yCords = arrayListOf<Int>()
        //fill yCords with 1 through size elements at their correct index
        for (i in 0 until size) {
            yCords.add(i)
        }
        //shuffle the ycords list
        yCords.shuffle()

        //fill points array with values in yCords
        for (i in 0 until size) {
            points[i] = yCords[i]
        }
        //call helper functions to display chart
        pointToEntry()
        setChartData()
    }

    /**
     * Helper function to initialize the chart
     */
    @SuppressLint("SetTextI18n")
    private fun initChart() {
        //get size from slider, set arrays to that size, empty arrays, and update text
        size = slider.value.toInt()
        barColorArray = ArrayList(size)
        points = IntArray(size)
        entries.clear()
        chart.clear()
        sliderText.text = "SORTING SIZE: $size"

        sleep = 1000 / ((size * (size + 1)) / 2)

        //add color "white" to all indices of barColorArray
        for (i in 0..size) {
            barColorArray.add(Color.rgb(255, 255, 255))
        }

        //Call helper function
        setStartingData(size)

        //draw chart
        chart.invalidate()
    }

    /**
     * helper function to set chart data and colors, then show the chart
     */
    private fun setChartData() {
        barDataSet = BarDataSet(entries, "")
        data = BarData(barDataSet)
        barDataSet.colors = barColorArray
        chart.data = data
        chart.invalidate()
    }

    /**
     * Helper function that takes all values in the points arraylist, and converts them
     * to chart data entries at correct index
     */
    private fun pointToEntry() {
        entries.clear()
        for (i in points.indices) {
            entries.add(BarEntry(i.toFloat()+1, points[i].toFloat()))
        }
    }


    /**
     * Helper function that finds the string resource that matches the corresponding
     * spinner choice
     *
     * @return the string resource that matches the selected item of the sort methods
     * spinner
     */
    fun getSpinnerText(): String {
        return when (sortMethods.selectedItemPosition) {
            0 -> resources.getString(R.string.bubbleSortInfo)
            1 -> resources.getString(R.string.selectionSortInfo)
            2 -> resources.getString(R.string.insertionSortInfo)
            3 -> resources.getString(R.string.quickSortInfo)
            4 -> resources.getString(R.string.mergeSortInfo)
            else -> "null"
        }
    }

    private fun getSpinnerTitle(): String {
        return when (sortMethods.selectedItemPosition) {
            0 -> SortTypes.BubbleSort.toString()
            1 -> SortTypes.SelectionSort.toString()
            2 -> SortTypes.InsertionSort.toString()
            3 -> SortTypes.QuickSort.toString()
            4 -> SortTypes.MergeSort.toString()
            else -> "null"
        }
    }

    /**
     * A helper function that is called whenever the sorting progress is finished, to
     * display an "animation" that indicates the sorting is done
     */
    private suspend fun sortingFinished() {
        //convert all sorted points
        pointToEntry()
        //for each bar, change it to the finished color and sleep 20 millis, to
        // "animate" all bars turning green
        for (i in 0 until size) {
            sleep(10)
            barColorArray[i] = fin
            //Show change for each bar color
            withContext(mainScope) {
                setChartData()
            }
        }

        //Wait 1500 millis, then reset the chart
        sleep(1500)
        withContext(mainScope) {
            slider.isEnabled = true
            sortChart.isClickable = true
            initChart()
        }
    }

    /**
     * Function that does bubble sort to the unsorted chart, and animates the process
     */
    private fun bubbleSort(){
        //launch the default scope
        GlobalScope.launch(defaultScope) {
            //disable slider in main scope
            withContext(mainScope) {
                sortChart.isClickable = false
                slider.isEnabled = false
            }
            var swap = true

            while(swap){
                swap = false
                //For each point, if a swap is necessary, do the swap
                for(i in 0 until points.size-1) {
                    if (points[i] > points[i+1]) {
                        val temp = points[i]
                        points[i] = points[i+1]
                        points[i + 1] = temp
                        //animate swap in main scope by changing colors
                    withContext(mainScope) {
                        barColorArray[i] = lb
                        barColorArray[temp] = purp
                        pointToEntry()
                        setChartData()
                      }
                        swap = true
                    }
                    sleep(sleep.toLong()/5)
                }
            }
            //Call the sorting finished process
            sortingFinished()
        }
    }

    /**
     * Function that does selection sort on the unsorted chart data, and visualizes the
     * process
     */
    private fun selectionSort() {
        //launch the default Score
        GlobalScope.launch(defaultScope) {
            //disable slider in main scope
            withContext(mainScope) {
                sortChart.isClickable = false
                slider.isEnabled = false
            }
            var minIndex: Int
            var tempInt: Int

            //For each point
            for (i in 0 until size) {
                //make the "minimum index" highlighted
                if (i > 0) {
                    withContext(mainScope) {
                        pointToEntry()
                        barColorArray[i] = lb
                        setChartData()
                    }
                }
                minIndex = i
                //For all elements after minimum index
                for (j in i+1 until size) {
                    //make points purple
                    withContext(mainScope) {
                        pointToEntry()
                        barColorArray[j] = purp
                        setChartData()
                    }
                    sleep(sleep.toLong())
                    //if the point is less than min index, swap them
                    if (points[j] < points[minIndex]) {
                        minIndex = j

                    }
                }
                //Swap the temporary minimum with the minimum value, and update color
                // of new minimum index
                tempInt = points[minIndex]
                points[minIndex] = points[i]
                points[i] = tempInt
                withContext(mainScope) {
                    barColorArray[minIndex] = lb
                    setChartData()
                }
            }
            //display finished animation
            sortingFinished()
        }
    }

    /**
     * Function that sorts the chart using insertion sort, and visualizes the
     * process as well.
     */
    private fun insertionSort() {
        //launch default Scope
        GlobalScope.launch(defaultScope) {
            //in Main Scope, disable slider
            withContext(mainScope) {
                slider.isEnabled = false
                sortChart.isClickable = false
            }
            //set two placeholder ints
            var key: Int
            var j by Delegates.notNull<Int>()
            //For each index
            for (i in 1 until size) {
                sleep(sleep.toLong())
                //set color to light blue
                withContext(mainScope) {
                    sleep(sleep.toLong())
                    pointToEntry()
                    barColorArray[i] = lb
                    setChartData()

                }
                // make key equal to value of the current index
                key = points[i]
                j = i-1
                //While the value is greater that the "key"s value, update color of
                // item to insert and progressively grow the sorted array
                while(j>=0 && points[j] > key) {
                    withContext(mainScope) {
                        pointToEntry()
                        barColorArray[j] = purp
                        setChartData()
                    }
                    sleep(sleep.toLong())
                    withContext(mainScope) {
                        withContext(mainScope) {
                            pointToEntry()
                            barColorArray[j] = lb
                        }

                    }
                    points[j+1] = points[j]
                    j--
                }
                points[j+1] = key
            }
            //display finishing animation
            sortingFinished()
        }
    }

    /**
     * Driver function to sort the chart using Quick Sort, and visualize the process as
     * it happens
     */
    private fun doQuickSort() {
        //launch the default scope
        GlobalScope.launch(defaultScope) {
            //disable slider in main scope
            withContext(mainScope) {
                slider.isEnabled = false
                sortChart.isClickable = false
            }
            //call quick sort
            quickSort(0, size-1)
            sortingFinished()
        }
    }

    /**
     * Suspend function that recursively calls quickSort while partitioning the array,
     * in order to sort the array
     */
    private suspend fun quickSort(low: Int, high: Int) {
        if (low < high) {
            val pi: Int = partition(low, high)
            for (i in low until pi) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[i] = lb
                    setChartData()
                }
            }
            for (i in pi+1 until high) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[i] = purp
                    setChartData()
                }
            }
            quickSort(low, pi-1)

            quickSort(pi+1, high)

        }
    }

    /**
     * The partition function of quick sort, which is called every time quickSort()
     * itself is called
     */
    private fun partition(low: Int, high: Int): Int {
        //make the pivot point the "high" value
        val pivot = points[high]
        sleep(sleep.toLong())
        var i = (low - 1)
        var tempInt: Int
        //make the values from j to the pivot points light blue
        for (j in low until high) {
            sleep(sleep.toLong())
            //If the value is less than the pivot
            if (points[j] < pivot) {
                i++
                //swap the value at point I and J
                tempInt = points[i]
                points[i] = points[j]
                points[j] = tempInt
                sleep(sleep.toLong())
            }
        }
        i++
        tempInt = points[i]
        points[i] = points[high]
        points[high] = tempInt
        sleep(sleep.toLong())

        return i
    }

    /**
     * This function takes the unsorted chart, and sorts it using merge sort, while
     * visualizing the process as well.
     */
    private fun doMergeSorting() {
        //launch default scope
       GlobalScope.launch(defaultScope) {
            //disable slider in main scope
            withContext(mainScope) {
                slider.isEnabled = false
                sortChart.isClickable = false
            }
            mergeSort(0, size-1)
            //play the finished sorting animation
            sortingFinished()
        }
    }
    /**
     * Suspend function which acts as the driver of the merge sort. It successively
     * splits the chart data and sorts each half until the arrays are length 1, then
     * uses the merge() function to compile the results
     */
    private suspend fun mergeSort(l: Int, r: Int) {
        if (l < r) {
            val m = l + (r-l) /2
            for (i in l until m) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[i] = lb
                    setChartData()
                }
            }
            for (i in m+1 until r) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[i] = purp
                    setChartData()
                }
            }
            mergeSort(l, m)
            mergeSort(m+1, r)
            merge(l, m, r)
        }
    }

    /**
     * Function that acts as the integral part of merge sort, the merging
     * feature that merges all the results together
     */
    private fun merge(l: Int, m: Int, r: Int) {
        //values for end point and midpoint, and two array lists
        val n1 = m-l+1
        val n2 = r - m
        val lis1 = ArrayList<Int>()
        val lis2 = ArrayList<Int>()
        //for all items from 0 to middle
        for (i in 0 until n1) {
            //add point to list 1, change color in main scope
            lis1.add(points[l+i])
            sleep(sleep.toLong())
        }
        //For points in 0 until n2
        for (j in 0 until n2) {
            //add points greater than midpoint to list 2, change color in main scope
            lis2.add(points[m+1+j])
            //sleep(sleep.toLong())
        }
        var i = 0
        var j = 0
        var k = l
        while (i < n1 && j < n2) {
            if (lis1[i] < lis2[j]) {
                points[k] = lis1[i]
                i++
            } else {
                points[k] = lis2[j]

                j++
            }
            k++
        }

        while (i < n1) {
            points[k] = lis1[i]
            k++
            i++
        }

        while (j<n2) {
            points[k] = lis2[j]
            k++
            j++
        }

    }

}