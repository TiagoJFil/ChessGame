import chess.domain.MoveType
import chess.domain.PieceMove
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class BishopTest {
    @Test
    fun `Bishop Move to the left up`() {
        val sut = Board().makeMove("Pb2b4")
        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Bishop Move to right up`() {
        val sut = Board().makeMove("Pg2g4")
        val startPos = Square(Column.F, Row.One)
        val endPos = Square(Column.H, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)

    }

    @Test
    fun `Bishop Move to left down`() {
        val sut = Board().makeMove("Pg2g4").makeMove("pg7g6").makeMove("Bf1h3")

        val startPos = Square(Column.H, Row.Three)
        val endPos = Square(Column.F, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)

    }

    @Test
    fun `Bishop Move to right down`() {
        val sut = Board().makeMove("Pe2e4").
        makeMove("pf7f6").
        makeMove("Bf1d3").
        makeMove("pf6f5")

        val startPos = Square(Column.D, Row.Three)
        val endPos = Square(Column.F, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)

    }

    @Test
    fun `Bishop cant Move trough a piece`() {
        val sut = Board()

        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }
}