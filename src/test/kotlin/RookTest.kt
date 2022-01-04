import chess.domain.MoveType
import chess.domain.PieceMove
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class RookTest {
    @Test
    fun `Rook Move up`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Rook cant move trough a piece`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Five)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Rook Move right`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.B, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Rook Move left`() {
        val sut = Board().makeMove("Ph2h4").makeMove("pg7g6").makeMove("ng1f3").makeMove("pg6g5")

        val startPos = Square(Column.H, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Rook Move down`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5").makeMove("Ra1a3")

        val startPos = Square(Column.A, Row.Three)
        val endPos = Square(Column.A, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun hasRookMoved() {
        val b = Board()
        val startPos = Square(Column.A, Row.One)
        val piece = b.getPiece(startPos) as Rook
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        b.makeMove("Pa2a3")
        b.makeMove("Ra1a2")
        val hasMoved = piece.hasMoved()
        assertTrue(hasMoved)
    }
    //falta fazer  teste para o castle
}