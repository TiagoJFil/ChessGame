import chess.domain.MoveType
import chess.domain.PieceMove
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class QueenTest {
    @Test
    fun `QUEEN Move vertically 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `QUEEN Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `QUEEN Captures a piece`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5").makeMove("qd1f3").makeMove("pg5g4")

        val startPos = Square(Column.F, Row.Three)
        val endPos = Square(Column.F, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.CAPTURE, move)
    }


    @Test
    fun `QUEEN cant move over pieces `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `QUEEN cant move to a friendly piece `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.C, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

}