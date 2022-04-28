package edu.bloomu.km25601.mpchart

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.github.mikephil.charting.charts.BarChart
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * A Utility object which contains functions that are called across many different
 * fragments and activities. Made to externalize code, and reduce reuse
 *
 * @author Karun Mahadevan
 */
@DelicateCoroutinesApi
object Utils {

    /**
     * Function that is called to set a gradient text of the "title" field of all the
     * fragments/activities
     *
     * @param text is the title message
     * @param tv is the text view of the title
     */
    fun setGradText(text: String, tv: TextView) {
        tv.text = text
        tv.textSize = 45f

        val paint = tv.paint
        val width = paint.measureText(text)

        tv.setTextColor(Color.parseColor("#70C7F0"))
        val textShader = LinearGradient(0f, 0f, width, tv.textSize,
            intArrayOf(Color.parseColor("#70C7F0"),
                Color.parseColor("#6C85DD"),
                Color.parseColor("#70C7F0")), null, Shader.TileMode.CLAMP)

        tv.paint.shader = textShader
    }

    /**
     * Utility function to set the width and height of a view
     *
     * @param view is the view being modified
     * @param w is the desired width
     * @param h is the desired height
     */
    fun setWH(view: View, w: Int, h: Int) {
        view.layoutParams.width = w
        view.layoutParams.height = h
    }

    /**
     * Utility function to set the width of a view
     *
     * @param view is the view being modified
     * @param w is the desired width
     */
    fun setW(view: View, w: Int) {
        view.layoutParams.width = w
    }

    /**
     * Utility function to set the height of a view
     *
     * @param view is the view being modified
     * @param h is the desired height
     */
    fun setH(view: View, h: Int) {
        view.layoutParams.height = h
    }

    /**
     * Utility function to set the properites of an MPAndroid Bar Chart
     *
     * @param chart is the bar chart being modified
     */
    fun chartProperties(chart: BarChart) {
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.setTouchEnabled(false)
        //hide grid lines
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawGridLines(false)
        chart.axisLeft.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.setDrawGridLines(false)

        chart.xAxis.setDrawAxisLine(false)
        //remove right y-axis
        chart.axisRight.isEnabled = false
        chart.axisLeft.isEnabled = false

        //remove legend
        chart.legend.isEnabled = false
        //remove description label
        chart.description.isEnabled = false
    }

    /**
     * Utility function to change fragments
     *
     * @param tran is the fragment transaction thats motivated the switch
     * @param fragment is the target fragment
     */
    fun replaceFrag(tran: FragmentTransaction, fragment: Fragment) {
        val homeLayout = R.id.homeLayout
        tran.replace(homeLayout, fragment)
        tran.commit()
    }

    /**
     * Utility function to switch from a fragment to the main activity
     *
     * @param frag is the current fragment
     */
    fun returnToHome(frag: Fragment) {
        val intent = Intent(frag.context, MainActivity::class.java)
        frag.startActivity(intent)
    }

}