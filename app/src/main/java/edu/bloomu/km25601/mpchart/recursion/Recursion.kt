@file:Suppress("BlockingMethodInNonBlockingContext")

package edu.bloomu.km25601.mpchart.recursion

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import edu.bloomu.km25601.mpchart.R
import edu.bloomu.km25601.mpchart.Utils
import kotlinx.coroutines.*
import java.lang.Thread.sleep

@DelicateCoroutinesApi
class Recursion : Fragment(), View.OnClickListener {
    //Lateinit views
    private lateinit var homeButton: Button
    private lateinit var recurTitle: TextView
    private lateinit var help: Button
    private lateinit var slider: Slider
    private lateinit var sliderText: TextView
    private lateinit var recur: Button
    private lateinit var table: TableLayout
    private lateinit var board: Array<IntArray>

    //Variables to be reassigned/used globally
    private var boardSize = 4
    private var width = 0
    private val queen = "\u265b"

    //Table of buttons for queens chessboard
    private var buttons = Array(boardSize) { arrayOfNulls<Button>(boardSize)}
    private var tableRowParams: TableRow.LayoutParams = TableRow.LayoutParams(
        TableRow.LayoutParams.WRAP_CONTENT,
        TableRow.LayoutParams.WRAP_CONTENT
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_recursion, container, false)

        width = resources.displayMetrics.widthPixels

        //Instantiating views
        homeButton = v.findViewById(R.id.recurHome)
        recurTitle = v.findViewById(R.id.recurTitle)
        recur = v.findViewById(R.id.recur)
        sliderText = v.findViewById(R.id.recurText)
        table = v.findViewById(R.id.recurTable)
        help = v.findViewById(R.id.recurHelp)
        slider = v.findViewById(R.id.recurSlider)

        //table properties
        Utils.setWH(table, width, width)

        //recurTitles properties
        Utils.setGradText("RECURSION", recurTitle)

        //recur properties
        Utils.setW(recur, (width * .5).toInt())

        //sliderText properties
        Utils.setW(sliderText, (width * .5).toInt())

        //slider properties
        slider.valueFrom = 4f
        slider.valueTo = 8f
        slider.stepSize = 1f
        slider.value = 4f
        slider.addOnChangeListener { _, _, _ -> initQueens() }

        //assigning onClick listeners
        homeButton.setOnClickListener(this)
        recur.setOnClickListener(this)
        help.setOnClickListener(this)

        //initialize queens
        initQueens()

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
            homeButton -> Utils.returnToHome(this)
            recur -> solveNQ()
            help -> Utils.replaceFrag(
                requireActivity().supportFragmentManager.beginTransaction
                    (), RecurHelp()
            )
        }
    }

    /**
     * Function that builds the "chess board" or buttons by iteratively adding buttons
     * to a table layout, and assigning background based on position.
     */
    private fun buildTable() {
        val buttonSize = width/boardSize
        for (i in buttons.indices) {
            //In each row, create a table row
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = tableRowParams

            for (j in buttons.indices) {
                //Format buttons
                buttons[i][j] = Button(requireContext())
                buttons[i][j]!!.minimumWidth = buttonSize
                buttons[i][j]!!.minimumHeight = buttonSize
                buttons[i][j]!!.maxHeight = buttonSize
                buttons[i][j]!!.text = ""
                buttons[i][j]!!.textSize =  (buttonSize*.5).toFloat()
                //Depending on position, assign background so it looks like a chess board
                if ((i+j) % 2 == 0) {
                    buttons[i][j]!!.setBackgroundResource(R.drawable.chess_bg_a)
                } else {
                    buttons[i][j]!!.setBackgroundResource(R.drawable.chess_bg_b)
                }
                tableRow.addView(buttons[i][j])
            }
            table.addView(tableRow)
        }
    }


    /**
     * Function that initializes the board to a certain size and displays it
     */
    @SuppressLint("SetTextI18n")
    private fun initQueens() {
        //Clear table nd read value from the sliver
        table.removeAllViews()
        boardSize = (slider.value).toInt()

        //setting text box and the baord to size
        sliderText.text = "NUMBER OF QUEENS: $boardSize"
        board = Array(boardSize) { IntArray(boardSize) }
        buttons = Array(boardSize) { arrayOfNulls(boardSize)}

        //building table and making it visible
        buildTable()
        table.visibility = View.VISIBLE
    }

    /**
     * Helper function to check if a board spot is safe "from perspective of an
     * attacking queen"
     */
    private fun isSafe(row: Int, col: Int): Boolean {
        var i = 0
        //If queen on same col
        while (i < col) {
            if (board[row][i] == 1) return false
            i++
        }

        i = row
        var j = col
        //If on diagonal
        while (i >= 0 && j >= 0) {
            if (board[i][j] == 1) return false
            i--
            j--
        }

        i = row
        j = col
        //If on other diagonal
        while (j >= 0 && i < boardSize) {
            if (board[i][j] == 1) return false
            i++
            j--
        }
        return true
    }

    /**
     * Driver function to solve the NQueens problem, that dynamically  updates the
     * board while solving
     */
    private suspend fun solveNQRecur(col: Int): Boolean {

        //Reached last row, problem solved!
        if (col >= boardSize) return true

        for (i in 0 until boardSize) {
            //If spot is safe on column, add a queen in main scope
            if (isSafe(i, col)) {
                board[i][col] = 1
                withContext(Dispatchers.Main) {
                    buttons[i][col]!!.text = queen
                }
                sleep(250)


                //If you can solve next column, return true
                if (solveNQRecur(col+1)) return true

                //remove queen from old spot, update board in main scope
                board[i][col] = 0
                withContext(Dispatchers.Main) {
                    buttons[i][col]!!.text = ""
                }
                sleep(250)

            }
        }

        return false
    }

    /**
     * driver function to solve N-Queens problem, that solves the problem in the IO
     * Scope but updates board in main
     */
    private fun solveNQ(){
        GlobalScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) {
                slider.isEnabled = false
            }
            if (!solveNQRecur(0)) {
                println("Solution does not exist")
            }
            sleep(1500)
            withContext(Dispatchers.Main) {
                slider.isEnabled = true
                initQueens()
            }
        }

    }

}