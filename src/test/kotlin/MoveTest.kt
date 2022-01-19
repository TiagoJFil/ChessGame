import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test



class PieceMoveTest {
    @Test
    fun `PieceMove toString`() {
        val pmove = PieceMove(Square(Column.D, Row.One), Square(Column.D, Row.Three))
        val sut = Board()
        assertEquals("Qd1d3", pmove.formatToString(sut))

    }

    @Test
    fun `String to PieceMove`() {
        assertThrows(IllegalArgumentException::class.java) {
            val pmove = "d1d3".formatToPieceMove()
        }
        val pmove = "Pd1d3".formatToPieceMove()
        assertEquals(pmove, PieceMove(Square(Column.D, Row.One), Square(Column.D, Row.Three)))
    }

}

class Move_DataClassTest {
    @Test
    fun `Move toString`() {
        val string = "Pa2a4"
        val move = Move(string)
        assertEquals(string,move.toString())
    }

    @Test
    fun `String to Move`() {
        val string = "Pa2a4"
        val move = Move(string)
        assertEquals(string,move.toString())
    }

    @Test
    fun `String  with extras to Move `() {
        assertThrows(IllegalArgumentException::class.java) {
            val string = "Pa2xa4=Q"
            val sut = Board()
            val move = string.toMove(sut)
        }
    }

    @Test
    fun `String without piece to Move`() {
        val string = "a2a5"
        val sut = Board()
        val move =  string.toMove(sut)
        assertEquals("Pa2a5",move.toString())
    }

    @Test
    fun `String not valid to Move`() {
        assertThrows(IllegalArgumentException::class.java) {
            val string = "Pa2a5a"
            val sut = Board()
            val move =  string.toMove(sut)
        }
    }

}
