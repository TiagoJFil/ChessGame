import chess.domain.Direction
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
        val newSquare ="b2".toSquare().addDirection(Direction(1,-1))
        assertEquals("c3".toSquare(), newSquare)
    }
    @Test
    fun `addDirection works with subtraction`() {
        val newSquare ="b2".toSquare().addDirection(Direction(-1,1))
        assertEquals("a1".toSquare(), newSquare)
    }

    @Test
    fun `addDirection returns null when out of board`() {
        val newSquare ="b2".toSquare().addDirection(Direction(-3,-3))
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

    @Test
    fun `add direction, out of board`() {
        val newSquare ="a1".toSquare().addDirection(Direction(-1,0)) // out of board left
        assertEquals(null, newSquare)
        val newSquare2 ="a1".toSquare().addDirection(Direction(0,1)) // out of board down
        assertEquals(null, newSquare2)
        val newSquare3 ="h8".toSquare().addDirection(Direction(1,0)) // out of board right
        assertEquals(null, newSquare3)
        val newSquare4 ="h8".toSquare().addDirection(Direction(0,-1)) // out of board up
        assertEquals(null, newSquare4)
    }

    @Test
    fun `add directionNotNull, out of the board returns error`(){
        assertThrows(IllegalArgumentException::class.java) {
            "a1".toSquare().addDirectionNotNull(Direction(-1,0)) // out of board left
            "a1".toSquare().addDirectionNotNull(Direction(0,1)) // out of board down
            "h8".toSquare().addDirectionNotNull(Direction(1,0)) // out of board right
            "h8".toSquare().addDirectionNotNull(Direction(0,-1)) // out of board up
        }
    }

}

