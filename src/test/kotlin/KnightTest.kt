import chess.domain.MoveType
import chess.domain.PieceMove
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class KnightTest {
    @Test
    fun `Knight Move 1`() {
        val sut = Board()

        val startPos = Square(Column.B, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Knight Move 2`() {
        val sut = Board().makeMove("Nb1a3").makeMove("ph7h6")

        val startPos = Square(Column.A, Row.Three)
        val endPos = Square(Column.C, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Knight not allowed move`() {
        val sut = Board()

        val startPos = Square(Column.G, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }
}