
package edu.bloomu.km25601.mpchart.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import edu.bloomu.km25601.mpchart.R
import edu.bloomu.km25601.mpchart.Utils
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * The "Search Help" fragment is a guide on how to use the searching feature,
 * constructed as a scroll view of photos and captions. Option to proceed to searching
 * or return home is available.
 */
@DelicateCoroutinesApi
class SearchHelp : Fragment(), View.OnClickListener {
    //lateinit views
    private lateinit var search: Button
    private lateinit var home: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_search_help, container, false)

        //Accessing necessary views
        val title: TextView = v.findViewById(R.id.searchHelpTitle)
        search = v.findViewById(R.id.returnToSearch)
        home = v.findViewById(R.id.searchHelpHome)
        val im1: ImageView = v.findViewById(R.id.searchim1)
        val im2: ImageView = v.findViewById(R.id.searchim2)
        val im3: ImageView = v.findViewById(R.id.searchim3)
        val im4: ImageView = v.findViewById(R.id.searchim4)
        val im5: ImageView = v.findViewById(R.id.searchim5)

        //Setting dimensions and text of views
        Utils.setGradText("SEARCHING GUIDE", title)

        val width = resources.displayMetrics.widthPixels
        Utils.setWH(im1, width, width)
        Utils.setWH(im2, width, width)
        Utils.setWH(im3, width, width)
        Utils.setWH(im4, width, width)
        Utils.setWH(im5, width, width)

        //Setting onClick listeners
        search.setOnClickListener(this)
        home.setOnClickListener(this)

        return v
    }

    /**
     * Overridden onClick listener that, depending on the view, performs the
     * correct respective action
     *
     * @param view is the view is being assigned an onClick listener
     */
    override fun onClick(view: View?) {
        when (view) {
            search -> {
                //Forward to search fragment
                Utils.replaceFrag(
                    requireActivity().supportFragmentManager.beginTransaction
                        (), Search()
                )
            }
            home -> {
                //return home
                Utils.returnToHome(this)
            }
        }
    }
}