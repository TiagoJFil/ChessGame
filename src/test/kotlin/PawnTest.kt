import chess.domain.MoveType
import chess.domain.PieceMove
import chess.domain.Player
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class PawnTest {
    @Test
    fun `Pawn moves 2 squares`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }

    @Test
    fun `Pawn cant move backwards`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.One)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)

    }

    @Test
    fun `Pawn cant move to right or left`() {
        val sut = Board()

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.C, Row.Two)
        val endPos2 = Square(column = Column.A,row = Row.Two)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        val move2 = piece.canMove(sut, PieceMove(startPos,endPos2))
        assertEquals(MoveType.ILLEGAL, move)
        assertEquals(MoveType.ILLEGAL,move2)

    }

    @Test
    fun `Pawn cant move to same colored pieces  `() {
        val sut = Board().makeMove("nb1a3").makeMove("Pg7g6")


        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }


    @Test
    fun `Pawn that moved 2 squares cant move again 2 squares`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.A, Row.Four)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, firstMove2Squares)

    }
    @Test
    fun `after a pawn moved 2 squares anothar pawn on the start can do the same`() {
        val sut = Board().makeMove("pa2a4").makeMove("pg7g6")

        val startPos = Square(Column.B, Row.Two)
        val endPos = Square(Column.B, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")


        val firstMove2Squares = piece.canMove(sut, PieceMove(startPos,endPos))

        assertEquals(MoveType.REGULAR, firstMove2Squares)

    }
    @Test
    fun `Pawn moves 1 squares white`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.A, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves 1 squares black`() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Seven)
        val endPos = Square(Column.A, Row.Six)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.REGULAR, move)
    }
    @Test
    fun `Pawn moves cant move diagonally without a piece to capture `() {
        val sut = Board()

        val startPos = Square(Column.A, Row.Two)
        val endPos = Square(Column.B, Row.Three)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }
    @Test
    fun `Pawn can move diagonally to capture a piece `() {
        val sut = Board().makeMove("Pa2a4").makeMove("Pg7g6").makeMove("Pa4a5").makeMove("Pg6g5").makeMove("Pa5a6").makeMove("Pg5g4")


        val startPos = Square(Column.A, Row.Six)
        val endPos = Square(Column.B, Row.Seven)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")

        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.CAPTURE, move)
    }

    @Test
    fun `Pawn cant move two squares when a piece is in front`(){
        val sut = Board().makeMove("Ng1f3").makeMove("pe7e6")
        val startPos = Square(Column.F, Row.Two)
        val endPos = Square(Column.F, Row.Four)
        val piece = sut.getPiece(startPos) ?: throw IllegalStateException("No piece at $startPos")
        val move = piece.canMove(sut, PieceMove(startPos,endPos))
        assertEquals(MoveType.ILLEGAL, move)
    }

    @Test
    fun `After moving pawn counter should be equal to one`(){
        val sut = Board().makeMove("Pa2a4")
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPiece(endPos)
        if(piece is Pawn) assertEquals(1, piece.getCounter())
    }

    @Test
    fun `After moving pawn counter should be equal to two`(){
        val sut = Board().makeMove("Pa2a4").makeMove("pa7a5")
        val endPos = Square(Column.A, Row.Four)
        val piece = sut.getPiece(endPos)
        if(piece is Pawn) assertEquals(2, piece.getCounter())

    }

    @Test
    fun `After moving black pawn counter should be equal to one`(){
        val sut = Board().makeMove("Pa2a4").makeMove("pa7a5")
        val endPos = Square(Column.A, Row.Five)
        val piece = sut.getPiece(endPos)
        if(piece is Pawn) assertEquals(1, piece.getCounter())

    }

    @Test
    fun `Testing canEmpassant algorithm`(){
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pd7d5")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos)
        assert(moves.contains(PieceMove((Square(Column.E,Row.Five)),(Square(Column.D,Row.Six)))))
    }

    @Test
    fun `Testing canEmpassant algorithm2`(){
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pf7f6").makeMove("Ph2h3").makeMove("pd7d5")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos)
        println(moves)
        assert(moves.size == 3)
    }

    @Test
    fun `Testing canEmpassant algorithm3`(){
        val sut = Board().makeMove("Pe2e4").makeMove("ph7h6").makeMove("Pe4e5").makeMove("pd7d5").makeMove("Ph2h3").makeMove("pf7f6")
        val endPos = Square(Column.E, Row.Five)
        val piece = sut.getPiece(endPos)
        val moves = piece!!.getPossibleMoves(sut,endPos)
        println(moves)
        assert(moves.size == 2)
    }

    @Test
    fun hasPawnMoved() {
        val b = Board()
        val startPos = Square(Column.A, Row.Two)
        val piece = b.getPiece(startPos) as Pawn
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        b.makeMove("Pa2a3")
        val hasMoved = piece.hasMoved()
        assertTrue(hasMoved)
    }

    @Test
    fun `Initial white pawn cant promote`(){
        val b = Board()
        val pawnInitialPos = Square(column = Column.A, row = Row.Two)
        val pawnFinalPos = Square(column = Column.A, row = Row.Four)
        val pawn = Pawn(player = Player.WHITE)
        val pawnMoveType = pawn.canMove(b,pieceInfo = PieceMove(pawnInitialPos,pawnFinalPos))
        assertNotEquals(MoveType.PROMOTION,pawnMoveType)
        assertEquals(MoveType.REGULAR,pawnMoveType)
    }

    @Test
    fun `White pawn piece can promote`(){
        val b = Board()
        val pawnInitialPos = Square(column = Column.A, row = Row.Seven)
        val pawnFinalPos = Square(column = Column.B, row = Row.Eight)
        val pawn = Pawn(player = Player.WHITE)
        val pawnMoveType = pawn.canMove(b,pieceInfo = PieceMove(pawnInitialPos,pawnFinalPos))
        assertEquals(MoveType.PROMOTION,pawnMoveType)
        assertNotEquals(MoveType.REGULAR,pawnMoveType)
    }

    @Test
    fun `Initial black pawn cant promote`(){
        val b = Board()
        val pawnInitialPos = Square(column = Column.A, row = Row.Seven)
        val pawnFinalPos = Square(column = Column.A, row = Row.Five)
        val pawn = Pawn(player = Player.BLACK)
        val pawnMoveType = pawn.canMove(b,pieceInfo = PieceMove(pawnInitialPos,pawnFinalPos))
        assertEquals(MoveType.REGULAR,pawnMoveType)
        assertNotEquals(MoveType.PROMOTION,pawnMoveType)
    }

    @Test
    fun `Black pawn piece can promote`(){
        val b = Board()
        val pawnInitialPos = Square(column = Column.B, row = Row.Two)
        val pawnFinalPos = Square(column = Column.A, row = Row.One)
        val pawn = Pawn(player = Player.BLACK)
        val pawnMoveType = pawn.canMove(b,pieceInfo = PieceMove(pawnInitialPos,pawnFinalPos))
        assertEquals(MoveType.PROMOTION,pawnMoveType)
        assertNotEquals(MoveType.REGULAR,pawnMoveType)
    }
}