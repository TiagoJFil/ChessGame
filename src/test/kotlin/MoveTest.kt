import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import chess.UI.Compose.App
import chess.domain.MoveType
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import chess.domain.canPieceMoveTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test




class PawnTest {
    @Test
    fun `Pawn moves 2 squares`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }
    @Test
    fun `Pawn that moved 2 squares cant move again 2 squares`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.A, Row.Four)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, firstMove2Squares)

    }
    @Test
    fun `after a pawn moved 2 squares anothar pawn on the start can do the same`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.B, Row.Four)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }
    @Test
    fun `Pawn moves 1 squares white`() {
        val sut = Board()


        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves 1 squares black`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Seven)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves cant move diagonally without a piece to capture `() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }
    @Test
    fun `Pawn can move diagonally to capture a piece `() {
        val sut = Board().makeMove("Pa2a4").makeMove("Pg7g6").makeMove("Pa4a5").makeMove("Pg6g5").makeMove("Pa5a6").makeMove("Pg5g4")


        val startPos = Square(Column.A, Row.Six)
        val endPos = Square(Column.B, Row.Seven)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.CAPTURE, move)
    }

    }

class BishopTest {
    @Test
    fun `Bishop Move to the left up`(){
        val sut = Board().makeMove("Pb2b4")
        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR,move)


    }

    @Test
    fun `Bishop Move to right up`(){
        val sut = Board().makeMove("Pg2g4")
        val startPos = Square(Column.F, Row.One)
        val endPos = Square(Column.H, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")



        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR,move)

    }

    @Test
    fun `Bishop Move to left down`(){
        val sut = Board().makeMove("Pg2g4").makeMove("pg7g6").makeMove("Bf1h3")

        val startPos = Square(Column.H, Row.Three)
        val endPos = Square(Column.F, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")



        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)

    }

    @Test
    fun `Bishop cant Move trough a piece`(){
        val sut = Board()

        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")



        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)

    }

}

class KnightTest {
    @Test
    fun `Knight Move 1`() {
        val sut = Board()

        val startPos = Square(Column.B, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `Knight Move 2`() {
        val sut = Board().makeMove("Nb1a3").makeMove("ph7h6")

        val startPos = Square(Column.A, Row.Three)
        val endPos = Square(Column.C, Row.Four)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
}

class RookTest {
    @Test
    fun `Rook Move up`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `Rook cant move trough a piece`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Five)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }
    @Test
    fun `Rook Move left`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.B, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }


    //falta fazer  teste para o castle
}

class KingTest {
    @Test
    fun `King Move 1 space up`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `King cant move 2 spaces`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }
    @Test
    fun `King Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }

    @Test
    fun `King tries to move illegally `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `King tries to move illegally to the side `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `King tries to castle right`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )
        assertEquals(MoveType.CASTLE,move)
    }

    @Test
    fun `King tries to castle right without the rook`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3").makeMove("ng8f6").makeMove("ph2h4").makeMove("nh7h6").makeMove("ph1h3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )
        assertEquals(MoveType.ILLEGAL,move)
    }

}

class QueenTest {
    @Test
    fun `QUEEN Move vertically 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `QUEEN Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }

    @Test
    fun `QUEEN Captures a piece`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5").makeMove("qd1f3").makeMove("pg5g4")

        val startPos = Square(Column.F, Row.Three)
        val endPos = Square(Column.F, Row.Seven)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.CAPTURE,move)
    }


    @Test
    fun `QUEEN cant move over pieces `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Six)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `QUEEN cant move to a friendly piece `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.C, Row.One)
        val piece = sut.getPieceAt(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }

}
/*
class Check(){
    @Test
    fun `is check`(){
        val b = Board().makeMove("Pe2e3").
        makeMove("pe7e6").
        makeMove("Pd2d3").
        makeMove("qd8g5").
            makeMove("Ke1e2").makeMove("qg5g4")
        assertTrue(getCheck(b))
    }
}
*/