import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerTest{
    @Test
    fun belongsToWhichPlayer() {
        val b = Board()
        val startPos = Square(Column.E, Row.One)
        val piece = b.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")  //White Player

        assertTrue(piece.belongsToWhitePlayer())
        assertFalse(!piece.belongsToWhitePlayer())

        val startPos1 = Square(Column.E, Row.Eight)
        val piece1 = b.getPiece(startPos1) ?: throw IllegalStateException("No piece at $startPos") //Black Player

        assertTrue(!piece1.belongsToWhitePlayer())
        assertFalse(piece1.belongsToWhitePlayer())

    }
}