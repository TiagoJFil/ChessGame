import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
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
            "d1d3".formatToPieceMove()
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
            string.toMove(sut)
        }
    }

    @Test
    fun `Move given is promotable`(){
        val sut = Board().makeMove("Ph2h4").makeMove("pg7g5")
            .makeMove("Ph4g5").makeMove("ng8h6").makeMove("Pg5g6")
            .makeMove("nh6g4").makeMove("Pg6g7").makeMove("ng4h2")
        val move = "pg7g8"
        assertTrue(sut.isTheMovementPromotable(move))
    }

}
