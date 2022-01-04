import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class KingTest {
    @Test
    fun `King Move 1 space up`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `King cant move 2 spaces because of pawn in front`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `King tries to move illegally `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King tries to move illegally to the side `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King tries to castle right`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos) )
        assertEquals(MoveType.CASTLE, move)
    }

    @Test
    fun `King tries to castle right without the rook`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3").makeMove("ng8f6").makeMove("ph2h4").makeMove("nh7h6").makeMove("ph1h3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos) )
        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `is king in check`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5").makeMove("Ke1e2").makeMove("bc8g4")
        assertEquals(true, isKingInCheck(sut, "Pd2d4".formatToPieceMove()))
    }


    @Test
    fun `test checkMate`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5").makeMove("Ke1e2").makeMove("bc8g4")
        assertEquals(false, isCheckMate(sut))
    }

    @Test
    fun `test checkMate true`(){
        val sut = Board().makeMove("Pf2f3").makeMove("pe7e5").makeMove("Pg2g4").makeMove("Qd8h4")
        assertEquals(true, isCheckMate(sut))
    }
    @Test
    fun `test checkMate true 2`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pf7f6").makeMove("Pd2d4").makeMove("pg7g5").makeMove("Qd1h5")
        assertEquals(true, isCheckMate(sut))
    }

    @Test
    fun hasKingMoved() {
        val b = Board()
        val startPos = Square(Column.E, Row.One)
        val piece = b.getPiece(startPos) as King
        val notMovedYet = piece.hasMoved()
        Assert.assertFalse(notMovedYet)
        b.makeMove("Pf2f3")
        b.makeMove("Qe1f2")
        val hasMoved = piece.hasMoved()
        Assert.assertTrue(hasMoved)
    }
}