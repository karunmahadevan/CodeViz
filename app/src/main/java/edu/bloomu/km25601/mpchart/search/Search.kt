@file:Suppress("BlockingMethodInNonBlockingContext")
package edu.bloomu.km25601.mpchart.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
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
import kotlin.math.sqrt

@DelicateCoroutinesApi
class Search : Fragment(), View.OnClickListener {

    //Views
    private lateinit var home: Button
    private lateinit var searchMethods: Spinner
    private lateinit var searchData: Button
    private lateinit var searchText: TextView
    private lateinit var searchInfo: TextView
    private lateinit var slider: Slider
    private lateinit var title: TextView
    private lateinit var helpButton: Button

    //objects needed for an MPAndroid bar chart
    private lateinit var chart: BarChart
    private lateinit var points: IntArray
    private lateinit var data: BarData
    private lateinit var barDataSet: BarDataSet
    private var barColorArray = ArrayList<Int>()
    private var entries: ArrayList<BarEntry> = ArrayList()

    //Placeholders to be instantiated on OnCreateView
    private var width = 0
    private var height = 0
    private var lb = 0
    private var purp = 0
    private var fin = 0
    private var size = 300

    //Coroutine Scopes
    private val defaultScope = Dispatchers.Default
    private val mainScope = Dispatchers.Main

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_search, container, false)

        //Initializing width and height fields
        width = resources.displayMetrics.widthPixels
        height = resources.displayMetrics.heightPixels

        //Initializing color values that are used throughout class
        lb = ContextCompat.getColor(requireContext(), R.color.bgGradEnd)

        purp = ContextCompat.getColor(requireContext(), R.color.bgGradStart)

        fin = ContextCompat.getColor(requireContext(), R.color.second)

        //Instantiating views
        slider = v.findViewById(R.id.searchSlider)
        home = v.findViewById(R.id.homeButtonDrawData)
        chart = v.findViewById(R.id.searchGraph)
        searchMethods = v.findViewById(R.id.searchMethods)
        title = v.findViewById(R.id.titleTextSearch)
        searchInfo = v.findViewById(R.id.searchInfo)
        helpButton = v.findViewById(R.id.searchHelpButton)
        searchText = v.findViewById(R.id.searchText)
        searchData = v.findViewById(R.id.sortData)

        //SearchMethods properties
        Utils.setW(searchMethods, (width * .3).toInt())

        //searchText properties
        Utils.setW(searchText, (width * .4).toInt())

        //searchData properties
        Utils.setW(searchData, (width * .3).toInt())

        //slider properties
        slider.isVisible = false
        slider.addOnChangeListener { _, _, _ -> updateSelected() }
        
        //chart properties
        Utils.chartProperties(chart)
        Utils.setWH(chart, width, (height * .75).toInt())

        //searchMethods properties
        searchMethods.adapter = ArrayAdapter(requireContext(),
            R.layout.spinnerviewb, SearchTypes.values())
        searchMethods.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                p2: Int,
                p3: Long
            ) {
                searchInfo.text = getSpinnerText()
                Utils.setGradText(getSpinnerTitle(), title)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                searchInfo.text = getSpinnerText()
                Utils.setGradText(getSpinnerTitle(), title)
            }
        }

        //Setting onClick listeners
        home.setOnClickListener(this)
        helpButton.setOnClickListener(this)
        searchData.setOnClickListener(this)

        //initialize chart
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
            home -> Utils.returnToHome(this)
            helpButton -> Utils.replaceFrag(
                requireActivity().supportFragmentManager.beginTransaction
                    (), SearchHelp()
            )
            searchData -> {
                searchData.isClickable = false
                when (searchMethods.selectedItemPosition) {
                    0 -> linearSearch(0)
                    1 -> optimLinear()
                    2 -> doBinarySearch(0, size-1)
                    3 -> doTernarySearch()
                    4 -> doJumpSearch()
                    5 -> doExponentialSearch()
                }
                searchInfo.text = getSpinnerText()
                Utils.setGradText(getSpinnerTitle(), title)
            }

        }
    }

    /**
     * Function that resets chart to initialized state, with values in order and all
     * bar colors white.
     */
    private fun initChart() {
        //resetting barColorArray
        barColorArray = ArrayList(size)

        //setting sliders
        slider.valueFrom = 0f
        slider.valueTo = (size-1).toFloat()
        slider.stepSize = 1f
        slider.isVisible = true

        //Resetting chart
        points = IntArray(size)
        entries.clear()
        chart.clear()

        for (i in 0..size) {
            barColorArray.add(Color.rgb(255, 255, 255))
        }
        setStartingData(size)
        barColorArray[slider.value.toInt()] = lb
        setChartData()
        chart.invalidate()
    }

    /**
     * Helper function to reset all the chart data
     */
    private fun setStartingData(size: Int) {
        for (i in 0 until size) {
            points[i] = i + 1
        }

        pointToEntry()
        setChartData()
    }

    /**
     * Helper function that sets the chart data and colors, then invalidates it
     */
    private fun setChartData() {
        barDataSet = BarDataSet(entries, "")
        data = BarData(barDataSet)
        barDataSet.colors = barColorArray
        chart.data = data
        chart.invalidate()
    }

    /**
     * Helper function that takes all the values in the points array, adn puts them in
     * the entries array at the correct index
     */
    private fun pointToEntry() {
        entries.clear()
        for (i in points.indices) {
            entries.add(BarEntry(i.toFloat() + 1, points[i].toFloat()))
        }
    }

    /**
     * Helper function that, based on the sliders value will update the corresponding
     * data item in the chart, and set set the chart data
     */
    @SuppressLint("SetTextI18n")
    private fun updateSelected() {
        val num = (slider.value).toInt()
        for (i in barColorArray.indices) {
            barColorArray[i] = Color.rgb(255, 255, 255)
        }
        barColorArray[num] = ContextCompat.getColor(requireContext(), R.color.bgGradEnd)
        searchText.text = "SEARCH FOR: ${num + 1}"
        setChartData()
    }

    /**
     * Function that gets the correct externalized string resource depending on the
     * spinner's selected item
     *
     * @return the correct string resource depending on spinner selection
     */
    private fun getSpinnerText(): String {
        return when (searchMethods.selectedItemPosition) {
            0 -> resources.getString(R.string.linearSearchInfo)
            1 -> resources.getString(R.string.optimLinearInfo)
            2 -> resources.getString(R.string.binarySearchInfo)
            3 -> resources.getString(R.string.ternarySearchInfo)
            4 -> resources.getString(R.string.jumpSearchInfo)
            5 -> resources.getString(R.string.expoSearchInfo)
            else -> "null"
        }
    }

    private fun getSpinnerTitle(): String {
        return when (searchMethods.selectedItemPosition) {
            0 -> SearchTypes.Linear.toString()
            1 -> SearchTypes.OptimLinear.toString()
            2 -> SearchTypes.Binary.toString()
            3 -> SearchTypes.Ternary.toString()
            4 -> SearchTypes.Jump.toString()
            5 -> SearchTypes.Expo.toString()
            else -> "null"
        }
    }

    /**
     * Helper function that resets all colors and displays chart
     */
    private fun clearColors() {
        pointToEntry()
        for (i in barColorArray.indices) {
            barColorArray[i] = Color.rgb(255, 255, 255)
        }
        setChartData()
    }

    /**
     * Function that performs a linear search on the chart, and search and results
     *
     * @param left is the starting index of the linear search. Not defaulted to 0
     * because jump search utilizes linear search not starting at 0
     */
    private fun linearSearch(left: Int) {
        //Set value to find
        val toFind = slider.value.toInt() + 2
        //Launch the default Scope Dispatcher
        GlobalScope.launch(defaultScope) {
            //In main scope, disable slider
            withContext(mainScope) {
                slider.isEnabled = false
            }
            var min = left
            //While the value is not found, increment search index and update chart in
            // Main scope
            while (points[min] != toFind) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[min] = purp
                    setChartData()
                }
                min++
            }
            //When found, change color of found value
            if (points[min] == toFind) {
                    barColorArray[toFind - 2] = fin

            }
            sleep(1500)
            //Reset chart
            withContext(mainScope) {
                slider.isEnabled = true
                searchData.isClickable = true
                initChart()
            }
        }
    }

    /**
     * Function that performs and visualizes optimum linear search, which is just
     * simple linear search from both ends of data
     */
    private fun optimLinear() {
        //Set value to find
        val toFind = slider.value.toInt() + 2
        //Launch default Scope
        GlobalScope.launch(defaultScope) {
            //In main scope, disable slider
            withContext(mainScope) {
                slider.isEnabled = false
            }
            var min = 0
            var max = points.size-1
            //While the min and max are not the value to find, increment and decrement
            // the respective values and update chart in main scope
            while (points[min] != toFind && points[max] != toFind) {
                withContext(mainScope) {
                    pointToEntry()
                    barColorArray[min] = purp
                    barColorArray[max] = purp
                    setChartData()
                }
                min++
                max--
            }
            //When either min or max is correct, update chart wtih correct colors
            if (points[min] == toFind) {
                withContext(mainScope) {
                    barColorArray[toFind - 2] = fin
                }
            }
            if (points[max] == toFind) {
                withContext(mainScope) {
                    barColorArray[toFind - 1] = purp
                    barColorArray[toFind - 2] = fin
                }
            }
            sleep(1500)
            //reset chart
            withContext(mainScope) {
                slider.isEnabled = true
                searchData.isClickable = true
                initChart()
            }
        }

    }

    /**
     * Function that performs and visualizes binary search within a specified range. It
     * does not always search the entire chart because exponential search uses binary
     * search within a specific range
     *
     * @param left is the left most index bound
     * @param right is the right most index bound
     */
    private fun doBinarySearch(left: Int, right: Int) {
        //Value to find
        val toFind = slider.value.toInt() + 1
        //Launch default Scope
        GlobalScope.launch(defaultScope) {
            //Call helper function to perform search
            val correctIdx = binarySearch(toFind, left, right)

            //With returned value of helper function, update chart correctly
            withContext(mainScope) {
                pointToEntry()
                barColorArray[correctIdx] = fin
                setChartData()
            }
            sleep(1500)
            //reset chart
            withContext(mainScope) {
                slider.isEnabled = true
                searchData.isClickable = true
                initChart()
            }
        }
    }

    /**
     * Suspend function that doBinarySearch() calls. Because binary search is
     * recursive, this is needed to call with different range bounds successively
     *
     * @param goal is the value to find
     * @param left is the left bound of the binary search
     * @param right is the right bound of the binary search
     * @return index of value when found
     */
    private suspend fun binarySearch(goal: Int, left: Int, right: Int): Int {
        val color = lb
        //If search range greater than 0, calculate midpoint and clear colors
        if (right >= 1) {
            val mid = left + (right - left) / 2
            withContext(mainScope) {
                clearColors()
            }
            sleep(200)

            //Set colors of chart within range being searched, update chart in main scope
            withContext(mainScope) {
                for (i in left..right) {
                    barColorArray[i] = color
                }
                pointToEntry()
                barColorArray[goal - 1] = Color.rgb(255, 0, 255)
                setChartData()
            }
            sleep(200)
            //Highlight the midpoint and value to search within the chart
            withContext(mainScope) {
                barColorArray[mid] = Color.rgb(0, 0, 0)
                barColorArray[goal - 1] = Color.rgb(255, 0, 255)
                setChartData()
            }
            sleep(200)

            //If value is found return value
            if (points[mid] == goal) {
                return mid
            }

            //If value is greater than goal, do binary search on new range
            if (points[mid] > goal) {
                return binarySearch(goal, left, mid)
            }

            //Do binary search on lesser range
            return binarySearch(goal, mid + 1, right)
        }
        return -1
    }

    /**
     * Function that performs and visualizes Ternary Search
     */
    private fun doTernarySearch() {
        //Set value to find
        val toFind = slider.value.toInt() + 1
        //launch default scope
        GlobalScope.launch(defaultScope) {
            //Calculate correct index, then update chart to show it
            val correctIdx = ternarySearch(toFind, 0, points.size-1)
            withContext(mainScope) {
                pointToEntry()
                barColorArray[correctIdx] = Color.rgb(0, 255, 0)
                setChartData()
            }
            sleep(1500)
            //reset chart
            withContext(mainScope) {
                slider.isEnabled = true
                searchData.isClickable = true
                initChart()
            }
        }
    }

    /**
     * Suspend function that is called by driver doTernary Search, which recursively
     * ternary-searches and updates chart as it progresses
     *
     * @param goal is the value to find
     * @param left is left bound of ternary search
     * @param right is right bound of ternary search
     * @return index of goal
     */
    private suspend fun ternarySearch(goal: Int, left: Int, right: Int): Int {
        val color = lb
        //Calculate mid1 and mid2, the "thirds" of the range, and clear chart colors
        if (right >= 1) {
            val mid1 = left + (right-left) / 3
            val mid2 = right - (right-left) / 3
            withContext(mainScope) {
                clearColors()
            }
            sleep(200)

            //In main scope, change color of range being searched and show goal value
            withContext(mainScope) {
                for (i in left..right) {
                    barColorArray[i] = color
                }
                pointToEntry()
                barColorArray[goal - 1] = purp
                setChartData()
            }
            sleep(200)
            //Highlight the midpoints and goal value, update chart in main scope
            withContext(mainScope) {
                barColorArray[mid1] = Color.rgb(0, 0, 0)
                barColorArray[mid2] = Color.rgb(0, 0, 0)
                barColorArray[goal - 1] = purp
                setChartData()
            }
            sleep(200)

            //If goal is either "midpoint", left, or right bound, return index
            if (points[mid1] == goal) {
                return mid1
            }

            if (points[mid2] == goal) {
                return mid2
            }

            if (points[left] == goal) {
                return left
            }

            if (points[right] == goal) {
                return right
            }

            //Depending on where goal is with respect to "midpoints" search in range
            return when  {
                goal < mid1 -> ternarySearch(goal, left, mid1 - 1)
                goal > mid2 -> ternarySearch(goal, mid2+1, right)
                else -> ternarySearch(goal, mid1 + 1, mid2 - 1)
            }
        }
        return -1
    }

    /**
     * Driver function for jump search, which performs and visualizes the search with
     * the help of a a helper function
     */
    private fun doJumpSearch() {
        //Value to find
        val toFind = slider.value.toInt() + 1
        //Jump interval is the square root of size for maximum efficiency
        val interval: Int = sqrt(size.toDouble()).toInt()
        //In default scope, call helper function jumpSearch()
        slider.isEnabled = false
        GlobalScope.launch(defaultScope) {
            jumpSearch(toFind, 0, interval)
            withContext(mainScope) {
                searchData.isClickable = true
                slider.isEnabled = true
                initChart()
            }
        }

    }

    /**
     * Suspend function which is the driver of the jump search. This recursively
     * increases the starting point and check if the goal is within the range. If it
     * is, it performs a linear search, otherwise it calls itself
     *
     * @param goal is the target to find
     * @param left is the leftmost bound
     * @param jump is the size of the range to jump
     */
    private suspend fun jumpSearch(goal: Int, left: Int, jump: Int) {
        val color = lb
        //In the main context, highlight the value to find and set chart
        withContext(mainScope) {
            barColorArray[goal - 1] = purp
            barDataSet.colors = barColorArray
            setChartData()
        }
        //Set all values below the goal to white
        withContext(mainScope) {
            for (i in points.indices) {
                if (i == goal-1) continue
                barColorArray[i] = Color.rgb(255, 255, 255)
            }
        }
        sleep(200)

        //Set the values in the jump range to color and update chart
        withContext(mainScope) {
            for (i in left..left+jump) {
                barColorArray[i] = color
            }
            pointToEntry()
            barColorArray[goal - 1] = purp
            barDataSet.colors = barColorArray
            setChartData()
        }
        sleep(200)
        //If target is in the jump range, perform linear search otherwise advance range
        if (left <= goal && goal < left + jump) {
            withContext(mainScope) {
                linearSearch(left)
            }
        } else {
            jumpSearch(goal, left+jump, jump)
        }
    }

    /**
     * Function that does and visualizes exponential search. Exponential search is
     * similar to jump search, but the range increases exponentially and binary search
     * is done instead of linear search within the target range.
     */
    private fun doExponentialSearch() {
        slider.isEnabled = false
        //Value to find
        val toFind = slider.value.toInt() + 1
        //launch default Scope, and call exponentialSearch() helper function
        GlobalScope.launch(defaultScope) {
            exponentialSearch(toFind)
            withContext(mainScope) {
                slider.isEnabled = true
                searchData.isClickable = true
                initChart()
            }
        }

    }

    /**
     * Suspend function which is the driver of exponential search
     *
     * @param goal is the target value to be found
     */
    private suspend fun exponentialSearch(goal: Int) {
        val color = lb
        //In main context, highlight value to find and update chart
        withContext(mainScope) {
            pointToEntry()
            barColorArray[goal - 1] = purp
            barDataSet.colors = barColorArray
            setChartData()
        }
        sleep(200)
        //Starting exponential value is 1
        var expo = 1

        //While goal is not in exponent range
        while (expo < goal) {
            sleep(200)
            //If exponent is greater than size, highlight up until end of chart, else
            // highlight entire exponent range
            withContext(mainScope) {
                if (expo > size) {
                    for (i in expo/2 until size) {
                        barColorArray[i] = color
                        barDataSet.colors = barColorArray
                        setChartData()
                    }
                } else {
                    for (i in expo/2..expo) {
                        barColorArray[i] = color
                        barDataSet.colors = barColorArray
                        setChartData()
                    }
                }
            }
            expo *= 2 //multiply exponent by 2
        }
        sleep(200)

        //If range greater than size, perform binary search on truncated range else do
        // binary search in exponent range
        if (expo > size) {
            doBinarySearch(expo/2, points.size-1)
        } else {
            doBinarySearch(expo/2, expo)
        }
    }
}