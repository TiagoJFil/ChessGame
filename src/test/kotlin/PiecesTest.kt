import chess.domain.King
import chess.domain.Pawn
import chess.domain.Rook
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class HasMoved {
  /*
    @Test
    fun hasPawnMoved() {
        val b = Board()
        val startPos = Square(Column.A, Row.Two)
        val piece = b.getPiece(startPos) as Pawn
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        val newBoard = b.makeMove("Pa2a3")
        val pawn = newBoard.getPiece(Square(Column.A, Row.Three)) as Pawn
        val hasMoved = pawn.hasMoved()
        assertTrue(hasMoved)
    }
*/
    @Test
    fun hasRookMoved() {
        val b = Board()
        val startPos = Square(Column.A, Row.One)
        val piece = b.getPiece(startPos) as Rook
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        val newBoard = b.makeMove("ph2h3").makeMove("pg7g6").makeMove("Rh1h2")
        val rook = newBoard.getPiece(Square(Column.H, Row.Two)) as Rook
        val hasMoved = rook.hasMoved()
        assertTrue(hasMoved)
    }
/*
    @Test
    fun hasKingMoved() {
        val b = Board()
        val startPos = Square(Column.E, Row.One)
        val piece = b.getPiece(startPos) as King
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        val newBoard = b.makeMove("Pf2f3").makeMove("pe7e6").makeMove("Ke1e2")
        val king = newBoard.getPiece(Square(Column.E, Row.Two)) as King
        val hasMoved = king.hasMoved()
        assertEquals(true,hasMoved)
    }
    */
}

class BelongToWhitePlayer() {
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