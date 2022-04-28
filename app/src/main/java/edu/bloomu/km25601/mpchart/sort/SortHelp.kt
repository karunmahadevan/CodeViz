package edu.bloomu.km25601.mpchart.sort

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import edu.bloomu.km25601.mpchart.R
import edu.bloomu.km25601.mpchart.Utils
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class SortHelp : Fragment(), View.OnClickListener {

    //lateinit views
    private lateinit var sort: Button
    private lateinit var home: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v: View = inflater.inflate(R.layout.fragment_sort_help, container, false)
        // Inflate the layout for this fragment
        val width = resources.displayMetrics.widthPixels

        //Accessing views
        val title: TextView = v.findViewById(R.id.sortHelpTitle)
        val im1: ImageView = v.findViewById(R.id.sortim1)
        val im2: ImageView = v.findViewById(R.id.sortim2)
        val im3: ImageView = v.findViewById(R.id.sortim3)
        val im4: ImageView = v.findViewById(R.id.sortim4)
        val im5: ImageView = v.findViewById(R.id.sortim5)
        sort = v.findViewById(R.id.returnToSort)
        home = v.findViewById(R.id.sortHelpHome)

        //Setting dimensions and text of views
        Utils.setWH(im1, width, width)
        Utils.setWH(im2, width, width)
        Utils.setWH(im3, width, width)
        Utils.setWH(im4, width, width)
        Utils.setWH(im5, width, width)

        Utils.setGradText("SORTING GUIDE", title)

        //Setting onClick listeners
        sort.setOnClickListener(this)
        home.setOnClickListener(this)

        return v
    }

    override fun onClick(view: View) {
        when (view) {
            sort -> {
                Utils.replaceFrag(
                    requireActivity().supportFragmentManager.beginTransaction
                        (), Sort()
                )
            }
            home -> {
                Utils.returnToHome(this)
            }
        }
    }
}