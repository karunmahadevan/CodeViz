package edu.bloomu.km25601.mpchart

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import edu.bloomu.km25601.mpchart.fastpath.PathHelp
import edu.bloomu.km25601.mpchart.recursion.RecurHelp
import edu.bloomu.km25601.mpchart.search.SearchHelp
import edu.bloomu.km25601.mpchart.sort.SortHelp
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * Main Activity of CodeViz, an app that visualizes many common and interesting coding
 * algorithm. This main activity is a "controller" acts as a landing page for a user to
 * choose which algorithm they want to explore.
 */
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity(), View.OnClickListener {
    //lateinit views that will be accessed outside scope of onCreate
    private lateinit var sortFrag: RelativeLayout
    private lateinit var graphFrag: RelativeLayout
    private lateinit var searchFrag: RelativeLayout
    private lateinit var recurFrag: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing necessary views
        sortFrag = findViewById(R.id.sortFragLayout)
        val sortText: TextView = findViewById(R.id.sortText)
        graphFrag= findViewById(R.id.graphFragLayout)
        val graphText: TextView = findViewById(R.id.graphText)
        searchFrag= findViewById(R.id.searchFragLayout)
        val searchText: TextView = findViewById(R.id.searchText)
        recurFrag= findViewById(R.id.recurFragLayout)
        val recurText: TextView = findViewById(R.id.recurText)

        val goalWidth = (resources.displayMetrics.widthPixels * .45).toInt()

        //Setting text and dimensions of views
        Utils.setGradText("SORTING", sortText)
        Utils.setGradText("FASTEST PATH", graphText)
        Utils.setGradText("SEARCHING", searchText)
        Utils.setGradText("RECURSION", recurText)

        Utils.setWH(sortFrag, goalWidth, goalWidth)
        Utils.setWH(graphFrag, goalWidth, goalWidth)
        Utils.setWH(searchFrag, goalWidth, goalWidth)
        Utils.setWH(recurFrag, goalWidth, goalWidth)

        //Setting onClick Listeners
        sortFrag.setOnClickListener(this)
        graphFrag.setOnClickListener(this)
        recurFrag.setOnClickListener(this)
        searchFrag.setOnClickListener(this)
    }

    /**
     * Overridden onClick method, because MainActivity implements View.OnClickListener.
     * This onClick removes all current views, then depending on the id of the item
     * clicked inflates the corresponding fragment using a Utils function.
     *
     * @param view is the view is being assigned an onClick listener
     */
    override fun onClick(view: View) {
        val homeLayout: ConstraintLayout = findViewById(R.id.homeLayout)
        homeLayout.removeAllViews()
        when (view) {
            sortFrag -> {Utils.replaceFrag(supportFragmentManager.beginTransaction(),
                SortHelp()
            )}
            graphFrag -> {Utils.replaceFrag(supportFragmentManager.beginTransaction(),
                PathHelp()
            )}
            searchFrag -> {Utils.replaceFrag(supportFragmentManager.beginTransaction(),
                SearchHelp()
            )}
            recurFrag -> {Utils.replaceFrag(supportFragmentManager.beginTransaction(),
                RecurHelp()
            )}
        }
    }
}