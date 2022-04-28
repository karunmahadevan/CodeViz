package edu.bloomu.km25601.mpchart.fastpath

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
class PathHelp : Fragment(), View.OnClickListener {
    private lateinit var path: Button
    private lateinit var home: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_path_help, container, false)
        // Inflate the layout for this fragment

        val width = resources.displayMetrics.widthPixels

        //Instantiating views
        path = v.findViewById(R.id.returnToPath)
        home = v.findViewById(R.id.pathHelpHome)
        val title: TextView = v.findViewById(R.id.pathHelpTitle)
        val im1: ImageView = v.findViewById(R.id.pathim1)
        val im2: ImageView = v.findViewById(R.id.pathim2)
        val im3: ImageView = v.findViewById(R.id.pathim3)
        val im4: ImageView = v.findViewById(R.id.pathim4)
        val im5: ImageView = v.findViewById(R.id.pathim5)


        Utils.setWH(im1, width, width)
        Utils.setWH(im2, width, width)
        Utils.setWH(im3, width, width)
        Utils.setWH(im4, width, width)
        Utils.setWH(im5, width, width)

        Utils.setGradText("FASTEST PATH GUIDE", title)

        //set onClick listeners
        home.setOnClickListener(this)
        path.setOnClickListener(this)

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
            home -> Utils.returnToHome(this)
            path -> Utils.replaceFrag(
                requireActivity().supportFragmentManager.beginTransaction
                    (), FPath()
            )
        }
    }

}