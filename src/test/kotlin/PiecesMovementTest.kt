import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PawnTest {
    @Test
    fun `Pawn moves 2 squares`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }

    @Test
    fun `Pawn cant move backwards`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to right`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to left`() {
        val sut = Board()

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.A, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to same colored pieces  `() {
        val sut = Board().makeMove("nb1a3").makeMove("Pg7g6")


        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }


    @Test
    fun `Pawn that moved 2 squares cant move again 2 squares`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.A, Row.Four)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, firstMove2Squares)

    }

    @Test
    fun `after a pawn moved 2 squares another pawn on the start can do the same`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.B, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }

    @Test
    fun `Pawn moves 1 square white`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Pawn moves 1 square black`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Seven)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Pawn moves cant move diagonally without a piece to capture `() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Pawn can move diagonally to capture a piece `() {
        val sut = Board().makeMove("Pa2a4").makeMove("Pg7g6")
            .makeMove("Pa4a5").makeMove("Pg6g5").makeMove("Pa5a6")
            .makeMove("Pg5g4")


        val startPos = Square(Column.A, Row.Six)
        val endPos = Square(Column.B, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.CAPTURE, move)
    }

    @Test
    fun `Pawn cant move when a piece is in front`() {
        val sut = Board().makeMove("Ng1f3").makeMove("pe7e6")

        val startPos = Square(Column.F, Row.Two)
        val endPos = Square(Column.F, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Testing canEnpassant algorithm`() {
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6")
            .makeMove("Pe4e5").makeMove("pd7d5")

        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut, endPos, false)
        assert(moves.contains(PieceMove((Square(Column.E, Row.Five)), (Square(Column.D, Row.Six)))))
    }

    @Test
    fun `Testing canEnpassant algorithm2`() {
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6")
            .makeMove("Pe4e5").makeMove("pf7f6").makeMove("Ph2h3")
            .makeMove("pd7d5")

        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut, endPos, verifyForCheck = false)
        assert(moves.size == 3)
    }

    @Test
    fun `Testing canEnpassant algorithm3`() {
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6")
            .makeMove("Pe4e5").makeMove("pd7d5").makeMove("Ph2h3")
            .makeMove("pf7f6")

        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut, endPos, verifyForCheck = false)
        assert(moves.size == 2)
    }

    @Test
    fun `Testing canEnpassant algorithm4`() {
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6")
            .makeMove("Pe4e5").makeMove("pf7f5")

        val initialPos = Square(column = Column.E,row = Row.Five)
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(initialPos) ?: throw IllegalStateException("No piece at $initialPos")
        val moves = piece.getPossibleMoves(sut, endPos, verifyForCheck = false)
        assert(moves.size == 2)
    }

    @Test
    fun `Pawn cant stop protecting king if king is going to be in check`() {
        val sut = Board()
            .makeMove("pa2a4").makeMove("pe7e5")
            .makeMove("pa4a5").makeMove("qd8h4")


        val startPos = Square(Column.F, Row.Two)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL,move)
    }

    @Test
    fun `Initial white pawn cant promote`() {
        val b = Board()
        val pawnInitialPos = Square(column = Column.A, row = Row.Two)
        val pawnFinalPos = Square(column = Column.A, row = Row.Four)
        val pawn = b.getPiece(pawnInitialPos) ?: throw IllegalStateException("No piece at $pawnFinalPos")
        val pawnMoveType = pawn.canMove(board = b, pieceInfo = PieceMove(pawnInitialPos, pawnFinalPos))
        assertNotEquals(MoveType.PROMOTION, pawnMoveType)
        assertEquals(MoveType.REGULAR, pawnMoveType)
    }

    @Test
    fun `White pawn piece can promote`() {
        val b = Board().makeMove("Ph2h4").makeMove("pg7g5")
            .makeMove("Ph4g5").makeMove("ng8h6").makeMove("Pg5g6")
            .makeMove("nh6g4").makeMove("Pg6g7").makeMove("ng4h2")

        val pawnInitialPos = Square(column = Column.G, row = Row.Seven)
        val pawnFinalPos = Square(column = Column.G, row = Row.Eight)
        val pawn = b.getPiece(at = pawnInitialPos) ?: throw IllegalStateException("No piece at $pawnInitialPos")
        val pawnMoveType = pawn.canMove(board = b, pieceInfo = PieceMove(pawnInitialPos, pawnFinalPos))
        assertEquals(MoveType.PROMOTION, pawnMoveType)
        assertNotEquals(MoveType.REGULAR, pawnMoveType)
    }

    @Test
    fun `Initial black pawn cant promote`() {
        val b = Board()
        val pawnInitialPos = Square(column = Column.A, row = Row.Seven)
        val pawnFinalPos = Square(column = Column.A, row = Row.Five)
        val pawn = b.getPiece(at = pawnInitialPos) ?: throw IllegalStateException("No piece at $pawnInitialPos")
        val pawnMoveType = pawn.canMove(b, pieceInfo = PieceMove(pawnInitialPos, pawnFinalPos))
        assertEquals(MoveType.REGULAR, pawnMoveType)
        assertNotEquals(MoveType.PROMOTION, pawnMoveType)
    }

    @Test
    fun `Black pawn piece can promote`() {
        val b = Board().makeMove("Ph2h4").makeMove("pb7b5")
            .makeMove("Ph4h5").makeMove("pb5b4").makeMove("Ph5h6")
            .makeMove("pb4b3").makeMove("Pg2g3").makeMove("pb3a2")
            .makeMove("Pg3g4")

        val pawnInitialPos = Square(column = Column.A, row = Row.Two)
        val pawnFinalPos = Square(column = Column.B, row = Row.One)
        val pawn = b.getPiece(at = pawnInitialPos) ?: throw IllegalStateException("No piece at $pawnInitialPos")
        val pawnMoveType = pawn.canMove(b, pieceInfo = PieceMove(pawnInitialPos, pawnFinalPos))
        assertEquals(MoveType.PROMOTION, pawnMoveType)
        assertNotEquals(MoveType.REGULAR, pawnMoveType)
    }
}

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
    fun `Bishop cant Move trough a piece`() {
        val sut = Board()

        val startPos = Square(Column.C, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Bishop cant stop protecting king if kign is going to be in check`() {
        val sut = Board()
            .makeMove("pa2a4").makeMove("pe7e5")
            .makeMove("pa4a5").makeMove("qd8g5")
            .makeMove("pb2b3").makeMove("qg5g2")
            .makeMove("pb3b4").makeMove("qg2g1")

        val startPos = Square(Column.F, Row.One)
        val endPos = Square(Column.G, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut,PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL,move)
    }

}

class KnightTest {
    @Test
    fun `Knight Move to the left`() {
        val sut = Board()

        val startPos = Square(Column.G, Row.One)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Knight Move regular to the right `() {
        val sut = Board().makeMove("Nb1a3").makeMove("ph7h6")

        val startPos = Square(Column.A, Row.Three)
        val endPos = Square(Column.C, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Knight not allowed move`() {
        val sut = Board()

        val startPos = Square(Column.G, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Knight captures a piece`() {
        val sut = Board().makeMove("Nb1c3").makeMove("pd7d5")

        val startPos = Square(Column.C, Row.Three)
        val endPos = Square(Column.D, Row.Five)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.CAPTURE,move)
    }


    @Test
    fun `Knight checks the king`() {
        val sut = Board().makeMove("Nb1c3").makeMove("pd7d5")
            .makeMove("Nc3e4").makeMove("pe7e5")


        val startPos = Square(Column.E, Row.Four)
        val endPos = Square(Column.D, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut,PieceMove(startPos,endPos))

        assertEquals(MoveType.CHECK,move)
    }



}

class RookTest {
    @Test
    fun `Rook Move up`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `Rook cant move trough a piece`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.A, Row.Five)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `Rook Move left`() {
        val sut = Board().makeMove("Pa2a4").makeMove("pg7g6").makeMove("nb1c3").makeMove("pg6g5")

        val startPos = Square(Column.A, Row.One)
        val endPos = Square(Column.B, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }


    //falta fazer  teste para o castle
}

class KingTest {
    @Test
    fun `King Move 1 space up`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6")
            .makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `King cant move 2 spaces`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6")
            .makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.E, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6")
            .makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `King tries to move illegally `() {
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6")
            .makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King tries to move illegally to the side `() {
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6")
            .makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.D, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `King tries to castle right`() {
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6")
            .makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")
            .makeMove("ng1f3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.CASTLE, move)
    }

    @Test
    fun `King tries to castle right without the rook`() {
        val sut = Board().makeMove("Pf2f4").makeMove("pg7g6")
            .makeMove("pe2e4").makeMove("pg6g5").makeMove("bf1e2")
            .makeMove("ng1f3").makeMove("ng8f6").makeMove("ph2h4")
            .makeMove("nh7h6").makeMove("ph1h3")

        val startPos = Square(Column.E, Row.One)
        val endPos = Square(Column.G, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `is king in check`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5")
            .makeMove("Ke1e2")

        assertEquals(true, isOpponentKingInCheckAfterMove(sut, "bc8g4".formatToPieceMove()))
    }


    @Test
    fun `test checkMate`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pd7d5")
            .makeMove("Ke1e2")

        val pmove = "bc8g4".formatToPieceMove()
        assertEquals(false, isCheckMateAfterMove(sut, pmove))
    }

    @Test
    fun `test checkMate true`() {
        val sut = Board().makeMove("Pf2f3").makeMove("pe7e5")
            .makeMove("Pg2g4")

        val pmove = "Qd8h4".formatToPieceMove()
        assertEquals(true, isCheckMateAfterMove(sut, pmove))
    }

    @Test
    fun `test checkMate true 2`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pf7f6")
            .makeMove("Pd2d4").makeMove("pg7g5")

        val pmove = "Qd1h5".formatToPieceMove()
        assertEquals(true, isCheckMateAfterMove(sut, pmove))
    }

    @Test
    fun `King can capture`(){
        val sut = Board().makeMove("Pf2f4").makeMove("pf7f5")
            .makeMove("Pe2e4").makeMove("pe7e5").makeMove("Pd2d4")
            .makeMove("pd7d5").makeMove("Pe4d5").makeMove("pe5f4")
            .makeMove("Pd5d6").makeMove("pf4f3").makeMove("Pd4d5")
            .makeMove("pf3f2")

        val initialPos = Square(column = Column.E, row = Row.One)
        val finalPos = Square(column = Column.F, row = Row.Two)
        val piece = sut.getPiece(at = initialPos) ?: throw IllegalStateException("No piece at $initialPos")
        val move = piece.canMove(board = sut, pieceInfo = PieceMove(initialPos,finalPos))

        assertEquals(MoveType.CAPTURE, move)
        assertNotEquals(MoveType.ILLEGAL, move)
    }
}

class QueenTest {
    @Test
    fun `QUEEN Move vertically 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `QUEEN Move diagonally 1 space`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.F, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.REGULAR, move)
    }

    @Test
    fun `QUEEN makes a check`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5").makeMove("qd1f3")
            .makeMove("pg5g4")

        val startPos = Square(Column.F, Row.Three)
        val endPos = Square(Column.F, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.CHECK, move)
    }

    @Test
    fun `QUEEN captures a piece`() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5").makeMove("pa2a3")
            .makeMove("pg5g4")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.G, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.CAPTURE, move)
    }


    @Test
    fun `QUEEN cant move over pieces `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.D, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `QUEEN cant move to a friendly piece `() {
        val sut = Board().makeMove("Pe2e4").makeMove("pg7g6").makeMove("pd2d4").makeMove("pg6g5")

        val startPos = Square(Column.D, Row.One)
        val endPos = Square(Column.C, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos, endPos))

        assertEquals(MoveType.ILLEGAL, move)
    }

}

