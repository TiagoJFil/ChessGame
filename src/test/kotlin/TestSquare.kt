import chess.domain.board_components.Square
import chess.domain.board_components.toColumn
import chess.domain.board_components.toRow
import org.junit.Assert.*
import org.junit.Test
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
        val square = "c2".toSquareOrNull()
        assertNotNull(square)
        assertEquals(2, square.column.ordinal)
        assertEquals(6, square.row.ordinal)
    }
    @Test
    fun `Invalid string to Square results null`() {
        assertNull("b3b".toSquareOrNull())
        assertNull("3b".toSquareOrNull())
        assertNull("x3".toSquareOrNull())
        assertNull("b9".toSquareOrNull())
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