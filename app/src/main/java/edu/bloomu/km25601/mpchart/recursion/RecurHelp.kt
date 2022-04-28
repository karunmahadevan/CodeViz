
package edu.bloomu.km25601.mpchart.recursion

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
class RecurHelp : Fragment(), View.OnClickListener {
    private lateinit var recur: Button
    private lateinit var home: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_recur_help, container, false)
        // Inflate the layout for this fragment
        val width = resources.displayMetrics.widthPixels

        //Initializing views
        val title: TextView = v.findViewById(R.id.recurHelpTitle)
        val im1: ImageView = v.findViewById(R.id.recurim1)
        val im2: ImageView = v.findViewById(R.id.recurim2)
        val im3: ImageView = v.findViewById(R.id.recurim3)
        recur = v.findViewById(R.id.returnToRecur)
        home = v.findViewById(R.id.recurHelpHome)

        //Setting dimensions and text
        Utils.setWH(im1, width, width)
        Utils.setWH(im2, width, width)
        Utils.setWH(im3, width, width)

        Utils.setGradText("RECURSION GUIDE", title)

        //Set onClick listeners
        recur.setOnClickListener(this)
        home.setOnClickListener(this)

        return v
    }

    override fun onClick(view: View?) {
        when (view) {
            recur -> Utils.replaceFrag(
                requireActivity().supportFragmentManager
                    .beginTransaction
                        (), Recursion()
            )
            home -> Utils.returnToHome(this)
        }
    }

}