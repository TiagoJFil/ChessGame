import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.assertEquals
import org.junit.Test




class PawnTest {
    @Test
    fun `Pawn moves 2 squares`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }

    @Test
    fun `Pawn cant move backwards`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to right`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to same colored pieces  `() {
        val sut = Board().makeMove("nb1a3").makeMove("Pg7g6")


        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }


    @Test
    fun `Pawn that moved 2 squares cant move again 2 squares`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.A, Row.Four)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, firstMove2Squares)

    }
    @Test
    fun `after a pawn moved 2 squares anothar pawn on the start can do the same`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.B, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }
    @Test
    fun `Pawn moves 1 squares white`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves 1 squares black`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Seven)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves cant move diagonally without a piece to capture `() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }
    @Test
    fun `Pawn can move diagonally to capture a piece `() {
        val sut = Board().makeMove("Pa2a4").makeMove("Pg7g6").makeMove("Pa4a5").makeMove("Pg6g5").makeMove("Pa5a6").makeMove("Pg5g4")


        val startPos = Square(Column.A, Row.Six)
        val endPos = Square(Column.B, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.CAPTURE, move)
    }

    @Test
    fun `Pawn cant move two squares when a piece is in front`(){
        val sut = Board().makeMove("Ng1f3").makeMove("pe7e6")
        val startPos = Square(Column.F, Row.Two)
        val endPos = Square(Column.F, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL,move)
    }



    @Test
    fun `Testing canEnpassant algorithm`(){
         val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pd7d5")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos,false)
        println(moves)
        assert(moves.contains(PieceMove((Square(Column.E,Row.Five)),(Square(Column.D,Row.Six)))))
    }

    @Test
    fun `Testing canEnpassant algorithm2`(){
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pf7f6").makeMove("Ph2h3").makeMove("pd7d5")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos,verifyForCheck = false)
        println(moves)
        assert(moves.size == 3)
    }

    @Test
    fun `Testing canEnpassant algorithm3`(){
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pd7d5").makeMove("Ph2h3").makeMove("pf7f6")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos, verifyForCheck = false)
        println(moves)
        assert(moves.size == 2)
    }

}

class BishopTest {
    @Test
    fun `Bishop Move to the left up`(){
        val sut = Board().makeMove("Pb2b4")
        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR,move)


    }

    @Test
    fun `Bishop Move to right up`(){
        val sut = Board().makeMove("Pg2g4")
        val startPos = Square(Column.F, Row.One)
        val endPos = Square(Column.H, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")



        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR,move)

    }

    @Test
    fun `Bishop Move to left down`(){
        val sut = Board().makeMove("Pg2g4").makeMove("pg7g6").makeMove("Bf1h3")

        val startPos = Square(Column.H, Row.Three)
        val endPos = Square(Column.F, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")



        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)

    }

    @Test
    fun `Bishop cant Move trough a piece`(){
        val sut = Board()

        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")



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
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `Knight Move 2`() {
        val sut = Board().makeMove("Nb1a3").makeMove("ph7h6")

        val startPos = Square(Column.A, Row.Three)
        val endPos = Square(Column.C, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }

    @Test
    fun `Knight not allowed move`() {
        val sut = Board()

        val startPos = Square(Column.G, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }


}

class RookTest {
    @Test
    fun `Rook Move up`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `Rook cant move trough a piece`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Five)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }
    @Test
    fun `Rook Move left`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.B, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

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
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `King cant move 2 spaces`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }
    @Test
    fun `King Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }

    @Test
    fun `King tries to move illegally `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `King tries to move illegally to the side `(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `King tries to castle right`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )
        assertEquals(MoveType.CASTLE,move)
    }

    @Test
    fun `King tries to castle right without the rook`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6").makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2").makeMove("ng1f3").makeMove("ng8f6").makeMove("ph2h4").makeMove("nh7h6").makeMove("ph1h3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos, endPos) )
        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `is king in check`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5").makeMove("Ke1e2").makeMove("bc8g4")
        assertEquals(true,isOpponentKingInCheckAfterMove(sut,"Pd2d4".formatToPieceMove()))
    }


    @Test
    fun `test checkMate`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5").makeMove("Ke1e2").makeMove("bc8g4")
        assertEquals(false,isCheckMateAfterMove(sut))
    }

    @Test
    fun `test checkMate true`(){
        val sut = Board().makeMove("Pf2f3").makeMove("pe7e5").makeMove("Pg2g4").makeMove("Qd8h4")
        assertEquals(true,isCheckMateAfterMove(sut))
    }
    @Test
    fun `test checkMate true 2`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pf7f6").makeMove("Pd2d4").makeMove("pg7g5").makeMove("Qd1h5")
        assertEquals(true,isCheckMateAfterMove(sut))
    }





}

class QueenTest {
    @Test
    fun `QUEEN Move vertically 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }
    @Test
    fun `QUEEN Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR,move)
    }

    @Test
    fun `QUEEN Captures a piece`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5").makeMove("qd1f3").makeMove("pg5g4")

        val startPos = Square(Column.F, Row.Three)
        val endPos = Square(Column.F, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.CAPTURE,move)
    }


    @Test
    fun `QUEEN cant move over pieces `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `QUEEN cant move to a friendly piece `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.C, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.ILLEGAL,move)
    }

}
