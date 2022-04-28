package edu.bloomu.km25601.mpchart

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.DelicateCoroutinesApi

@Suppress("DEPRECATION")
@DelicateCoroutinesApi
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var chart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        chart = findViewById(R.id.splashChart)

        val width = resources.displayMetrics.widthPixels

        Utils.setWH(chart, (width*.7).toInt(), width)

        val entries = ArrayList<BarEntry>()
        val colorEntries = ArrayList<Int>()

        for (i in 1 .. 100) {
            colorEntries.add(genColor(101-i))
            entries.add(BarEntry(i.toFloat(), i.toFloat()))

        }
        
        val barDataSet = BarDataSet(entries, "")
        barDataSet.colors = colorEntries
        val data = BarData(barDataSet)
        chart.data = data

        Utils.chartProperties(chart)

        chart.animateY(2000)
        chart.animateX(2000)
        chart.invalidate()

        // Deprecated, but it is impossible to but a custom widget in a splash screen
        // without using this method. Deprecation suppressed
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            startActivity(Intent(this,MainActivity::class.java))

            // close this activity
            finish()
        }, 2500)
    }

    /**
     * Function to generate the colors to make the bar chart have a gradient color scheme
     *
     * @param ratio is the ratio of first-color/second-color for the transition
     */
    private fun genColor(ratio: Int): Int {

        //second color rgb
        val r2 = 98
        val g2 = 33
        val b2 = 189

        //first color rgb
        val r1 =112
        val g1 = 199
        val b1 = 240

        //finding the step between colors, for size 100
        val rStep = ((r2-r1)/100.0)
        val gStep = ((g2-g1)/100.0)
        val bStep = ((b2-b1)/100.0)


        //setting color and returning it
        val red = (r1 + (rStep * ratio)).toInt()
        val green =  (g1 + (gStep * ratio)).toInt()
        val blue = (b1 + (bStep * ratio)).toInt()
        return Color.rgb(red, green, blue)
    }
}