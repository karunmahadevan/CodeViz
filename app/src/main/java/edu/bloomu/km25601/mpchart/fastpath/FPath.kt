package edu.bloomu.km25601.mpchart.fastpath

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import edu.bloomu.km25601.mpchart.R
import edu.bloomu.km25601.mpchart.Utils
import edu.bloomu.km25601.mpchart.recursion.RecurHelp
import kotlinx.coroutines.*
import java.util.*

@DelicateCoroutinesApi
class FPath : Fragment(), View.OnClickListener {

    //Initializing views
    private lateinit var drawGraphButton: Button
    private lateinit var homeButton: Button
    private lateinit var help: Button
    private lateinit var title: TextView
    private lateinit var info: TextView
    private lateinit var drawGraphLayout: RelativeLayout
    private lateinit var frameLayout: FrameLayout
    private lateinit var myCanvasView: MyCanvasView
    private lateinit var newTrack: GenerateTrack
    private lateinit var table: TableLayout

    //Information for the path and grid being searched to
    private var newPointsArray = ArrayList<Pair<Int, Int>>()
    private var buttons = Array(25) { arrayOfNulls<ImageButton>(25)}
    private var tableRowParams: TableRow.LayoutParams = TableRow.LayoutParams(
        TableRow.LayoutParams.WRAP_CONTENT,
        TableRow.LayoutParams.WRAP_CONTENT
    )

    private var formattedMap = ""

    //Variables to represent the "weight" of the path
    private var STRAIGHT_LENGTH = 2
    private var DIAGONAL_LENGTH = 4

    private val defaultScope = Dispatchers.Default

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_fpath, container, false)

        //Instantiating views
        drawGraphLayout = v.findViewById(R.id.drawGraphCanvasLayout)
        frameLayout = v.findViewById(R.id.GraphCanvas)
        drawGraphButton = v.findViewById(R.id.DrawGraphCanvasButton)
        homeButton = v.findViewById(R.id.homeButtonDrawGraph)
        help = v.findViewById(R.id.fastPathHelp)
        title = v.findViewById(R.id.titleTextPath)
        info = v.findViewById(R.id.fastestPathInfo)

        //Creating instance of custom view, and setting properties
        myCanvasView = activity?.let { MyCanvasView(it) }!!
        frameLayout.addView(myCanvasView)
        myCanvasView.contentDescription = "getString(R.string.description)"
        Utils.setWH(myCanvasView, 500, 500)

        //making the table
        table = TableLayout(context)

        //setting title
        Utils.setGradText("FASTEST PATH", title)

        //info text
        info.text = resources.getString(R.string.convert_to_map_text)

        //Setting onClick listeners
        homeButton.setOnClickListener(this)
        drawGraphButton.setOnClickListener(this)
        help.setOnClickListener(this)

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
            help -> Utils.replaceFrag(
                requireActivity().supportFragmentManager.beginTransaction
                    (), PathHelp()
            )
            drawGraphButton -> {
                when (drawGraphButton.text) {
                    resources.getString(R.string.convert_to_map) -> {
                        info.text = resources.getString(R.string.search_for_path_text)
                        table.removeAllViews()
                        newPointsArray.clear()

                        for (i in 0 until myCanvasView.touchPoints.size) {
                            val adjustedX = (myCanvasView.touchPoints[i].first).toInt()/20
                            val adjustedY = (myCanvasView.touchPoints[i].second).toInt()/20

                            val newPair: Pair<Int, Int> = Pair(adjustedX, adjustedY)
                            newPointsArray.add(newPair)
                        }
                        newTrack = GenerateTrack(newPointsArray)
                        newTrack.findBoundaries()
                        frameLayout.removeAllViews()
                        frameLayout.addView(table)

                        buildTable()
                        displayBoard()

                        drawGraphButton.text = resources.getString(R.string.search_for_path)
                    }

                    resources.getString(R.string.search_for_path) -> {
                        drawGraphButton.isClickable = false
                        info.text = resources.getString(R.string.draw_fastest_path_text)
                        formattedMap = ""

                        for (i in 0 until newTrack.totalSpots.size) {
                            for (j in 0 until newTrack.totalSpots.size) {
                                formattedMap += newTrack.totalSpots[i][j]
                            }
                            formattedMap += "\r"
                        }
                        coroutineaddPath(formattedMap)
                        drawGraphButton.text = resources.getString(R.string.draw_path)
                    }

                    resources.getString(R.string.draw_path) -> {
                        info.text = resources.getString(R.string.reset_fastest_text)
                        val temp = addPath(formattedMap)

                        val tempList = temp.lines()
                        for ((lineCount, i) in tempList.withIndex()) {
                            if (lineCount == 25) {
                                break
                            }
                            for (j in 0..24) {
                                when (i[j]) {
                                    '*' -> {
                                        adjustBoardPair(Pair(lineCount, j),
                                            R.drawable.finished_path
                                        )
                                    }
                                }
                            }
                        }
                        drawGraphButton.text = resources.getString(R.string.reset_path)
                    }

                    resources.getString(R.string.reset_path) -> {
                        info.text = resources.getString(R.string.convert_to_map_text)
                        frameLayout.removeAllViews()

                        myCanvasView = activity?.let { MyCanvasView(it) }!!

                        frameLayout.addView(myCanvasView)
                        myCanvasView.contentDescription = "getString(R.string.description)"

                        myCanvasView.layoutParams.width = 500
                        myCanvasView.layoutParams.height = 500

                        drawGraphButton.text = resources.getString(R.string.convert_to_map)
                    }
                }
            }

        }
    }

    private fun buildTable() {
        for (i in buttons.indices) {
            val tableRow = TableRow(requireContext())
            tableRow.layoutParams = tableRowParams

            for (j in buttons.indices) {
                buttons[i][j] = ImageButton(requireContext())
                buttons[i][j]!!.tag = Pair(i, j)
                buttons[i][j]!!.setBackgroundResource(R.drawable.testbg)
                tableRow.addView(buttons[i][j])
            }
            table.addView(tableRow)
        }
    }

    private fun displayBoard() {
        for (i in newPointsArray) {
            if (i.first < 0 || i.first > 24 || i.second < 0 || i.second > 24) {
                continue
            }
            buttons[i.first][i.second]!!.setBackgroundResource(R.drawable.testbg_1)
        }

        buttons[0][0]!!.setBackgroundResource(R.drawable.start)
        buttons[24][24]!!.setBackgroundResource(R.drawable.finish)
    }

    private fun adjustBoardPair(pair: Pair<Int, Int>, drawableID: Int) {
        buttons[pair.first][pair.second]!!.setBackgroundResource(drawableID)
    }

    private fun addPath(map: String): String {

        return getMapWithPath(map)
    }

    private fun coroutineaddPath(map: String): String {

        return coroutinegetMapWithPath(map)
    }

    private fun getMapWithPath(map: String): String {
        val (nodes, arcs) = readGraph(map)
        val start = nodes.find { it.isStart } ?: throw IllegalArgumentException("No start point specified")
        val end = nodes.find { it.isEnd } ?: throw IllegalArgumentException("No end point specified")

        val paths = calcDistances(start, nodes, arcs)

        return map.lines()
            .map(String::toCharArray)
            .let { charGrid ->
                charGrid[start.row][start.col] = '*'
                paths.getValue(end).forEach {
                    charGrid[it.row][it.col] = '*'
                }
                charGrid.joinToString(separator = "\n") { row -> row.joinToString(separator = "") }
            }
    }

    private fun coroutinegetMapWithPath(map: String): String {
        val (nodes, arcs) = readGraph(map)
        val start = nodes.find { it.isStart } ?: throw IllegalArgumentException("No start point specified")
        val end = nodes.find { it.isEnd } ?: throw IllegalArgumentException("No end point specified")

        val paths = coroutinecalcDistances(start, nodes, arcs)

        GlobalScope.launch(defaultScope) {
            withContext(Dispatchers.Main) {
                for (path in paths) {
                        adjustBoardPair(Pair(path.key.row, path.key.col),
                            R.drawable.testbg_2
                        )
                    }
                }

            map.lines()
                .map(String::toCharArray)
                .let { charGrid ->
                    charGrid[start.row][start.col] = '*'
                    paths.getValue(end).forEach {
                        charGrid[it.row][it.col] = '*'
                    }
                    charGrid.joinToString(separator = "\n") { row -> row.joinToString(separator = "") }
                }
        }


        return map.lines()
            .map(String::toCharArray)
            .let { charGrid ->
                charGrid[start.row][start.col] = '*'
                paths.getValue(end).forEach {
                    charGrid[it.row][it.col] = '*'
                }
                charGrid.joinToString(separator = "\n") { row -> row.joinToString(separator = "") }
            }
    }


    private fun calcDistances(
        start: Node,
        nodes: Collection<Node>,
        arcs: Map<Node, List<Arc>>
    ): Map<Node, List<Node>> {
        val paths = nodes.associateWith { emptyList<Node>() }.toMutableMap()
        val distances =
            nodes.associateWith { if (it == start) 0 else Int.MAX_VALUE }.toMutableMap()
        val visited = mutableSetOf<Node>()

        val queue = PriorityQueue<Node>(nodes.size) { n1, n2 ->
            distances.getValue(n1) - distances.getValue(n2)
        }
        queue.addAll(nodes)

            while (queue.isNotEmpty()) {
                val node = queue.poll()
                visited.add(node as Node)
                arcs.getValue(node)
                    .filterNot { visited.contains(it.node) }
                    .forEach { arc ->
                        if (distances.getValue(node) + arc.length < distances.getValue(arc.node)) {
                            distances[arc.node] = distances.getValue(node) + arc.length
                            paths[arc.node] = paths.getValue(node) + arc.node
                            queue.remove(arc.node)
                            queue.add(arc.node)
                        }
                    }
            }
        return paths.toMap()
    }

    private fun coroutinecalcDistances(
        start: Node,
        nodes: Collection<Node>,
        arcs: Map<Node, List<Arc>>
    ): Map<Node, List<Node>> {
        val paths = nodes.associateWith { emptyList<Node>() }.toMutableMap()
        val distances =
            nodes.associateWith { if (it == start) 0 else Int.MAX_VALUE }.toMutableMap()
        val visited = mutableSetOf<Node>()

        val queue = PriorityQueue<Node>(nodes.size) { n1, n2 ->
            distances.getValue(n1) - distances.getValue(n2)
        }
        queue.addAll(nodes)
            GlobalScope.launch(defaultScope) {
                while (queue.isNotEmpty()) {
                    val node = queue.poll()
                    visited.add(node as Node)
                withContext(Dispatchers.Main) {
                    adjustBoardPair(Pair(node.row, node.col), R.drawable.testbg_2)
                }


                    arcs.getValue(node)
                        .filterNot { visited.contains(it.node) }
                        .forEach { arc ->
                            if (distances.getValue(node) + arc.length < distances.getValue(arc.node)) {
                                distances[arc.node] = distances.getValue(node) + arc.length
                                withContext(Dispatchers.Main) {
                                    adjustBoardPair(Pair(arc.node.row, arc.node.col),
                                        R.drawable.finish
                                    )
                                }
                                paths[arc.node] = paths.getValue(node) + arc.node

                                queue.remove(arc.node)
                                queue.add(arc.node)
                                withContext(Dispatchers.Main) {
                                    adjustBoardPair(
                                        Pair(node.row, node.col), R.drawable.testbg
                                    )
                                }

                            }
                        }
                }
                withContext(Dispatchers.Main) {
                    drawGraphButton.isClickable = true
                }
            }
        return paths.toMap()
    }

    private fun readGraph(map: String): Pair<List<Node>, Map<Node, List<Arc>>> {
        val nodes = map.lines()
            .mapIndexed { row, str ->
                str.mapIndexedNotNull { col, char -> if (char == 'B') null else Node(row, col, char) }
            }
            .flatten()

        val arcs = nodes.associateWith { node ->
            val row = nodes.filter { it.row == node.row }
            val topRow = nodes.filter { it.row == node.row - 1 }
            val bottomRow = nodes.filter { it.row == node.row + 1 }
            val nodeArcs = listOfNotNull(
                topRow.find { it.col == node.col }?.let { Arc(it, STRAIGHT_LENGTH) },
                topRow.find { it.col == node.col - 1 }?.let { Arc(it, DIAGONAL_LENGTH) },
                topRow.find { it.col == node.col + 1 }?.let { Arc(it, DIAGONAL_LENGTH) },

                bottomRow.find { it.col == node.col }?.let { Arc(it, STRAIGHT_LENGTH) },
                bottomRow.find { it.col == node.col - 1 }
                    ?.let { Arc(it, DIAGONAL_LENGTH) },
                bottomRow.find { it.col == node.col + 1 }
                    ?.let { Arc(it, DIAGONAL_LENGTH) },

                row.find { it.col == node.col - 1 }?.let { Arc(it, STRAIGHT_LENGTH) },
                row.find { it.col == node.col + 1 }?.let { Arc(it, STRAIGHT_LENGTH) }
            )
            nodeArcs
        }

        return Pair(nodes, arcs)
    }

    class Node(val row: Int, val col: Int, char: Char) {

        val isStart = char == 'S'
        val isEnd = char == 'X'

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Node) return false

            if (row != other.row) return false
            if (col != other.col) return false

            return true
        }

        override fun hashCode(): Int {
            var result = row
            result = 31 * result + col
            return result
        }

    }
    class Arc(val node: Node, val length: Int)
}
