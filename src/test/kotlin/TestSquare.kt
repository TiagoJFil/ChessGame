import chess.domain.board_components.*
import org.junit.Assert.*
import org.junit.Test
import java.lang.IllegalArgumentException

/**
 * The Square type identifies a position on the board (Column and Row)
 * Squares are identified by one letter and one digit. The top left is "a8"
 */

class TestSquare {
    @Test
    fun `Create a square and convert to string`() {
        val square = Square(6.toColumn(), 1.toRow())
        assertEquals("g7", square.toString())
    }
    @Test
    fun `String to Square and use ordinal values`() {
        val square = "c2".toSquare()

        assertEquals(2, square.column.ordinal)
        assertEquals(6, square.row.ordinal)
    }
    @Test
    fun `addDirection works with sum`() {
        val newSquare ="b2".toSquare().addDirection(Pair(1,-1))
        assertEquals("c3".toSquare(), newSquare)
    }
    @Test
    fun `addDirection works with subtraction`() {
        val newSquare ="b2".toSquare().addDirection(Pair(-1,1))
        assertEquals("a1".toSquare(), newSquare)
    }

    @Test
    fun `addDirection returns null when out of board`() {
        val newSquare ="b2".toSquare().addDirection(Pair(-3,-3))
        assertEquals(null, newSquare)
    }

    @Test
    fun `Invalid string to Square results null`() {
        assertThrows(IllegalArgumentException::class.java) {
       "b3b".toSquare()
        "3b".toSquare()
        "x3".toSquare()
        "b9".toSquare()
        }
    }
    /*
    @Test
    fun `All valid squares`() {
        val all = Square.values
        assertEquals(8 * 8, all.size)
        assertEquals("a8", all.first().toString())
        assertEquals("h1", all.last().toString())
    }
    */
}

