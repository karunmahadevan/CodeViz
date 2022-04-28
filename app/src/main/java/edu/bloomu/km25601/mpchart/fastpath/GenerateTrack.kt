package edu.bloomu.km25601.mpchart.fastpath

/**
 *
 *
 * @author Karun Mahadevan
 */
class GenerateTrack(linePoints: ArrayList<Pair<Int, Int>>) {
    private val points: ArrayList<Pair<Int, Int>> = linePoints

    val size = 25
    var totalSpots = Array(size) {Array(size) {"."} }

    fun findBoundaries() {
        for (i in 0 until points.size-1) {
            val p1: Pair<Int, Int> = Pair(points[i].first, points[i].second)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    val boardPair = Pair(x, y)

                    if (boardPair == p1) {
                        totalSpots[x][y] = "B"
                    }
                }
            }
        }
        totalSpots[0][0] = "S"
        totalSpots[24][24] = "X"
    }

    override fun toString(): String {
        var help = ""
        for (i in 0 until size) {
            for (j in 0 until size) {
                help += totalSpots[i][j]
                help += "  "
            }
            help += "\n"
        }

        return help
    }
}