import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasMoved {
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

    @Test
    fun hasKingMoved() {
        val b = Board()
        val startPos = Square(Column.E, Row.One)
        val piece = b.getPiece(startPos) as King
        val notMovedYet = piece.hasMoved()
        assertFalse(notMovedYet)
        b.makeMove("Pf2f3")
        b.makeMove("Qe1f2")
        val hasMoved = piece.hasMoved()
        assertTrue(hasMoved)
    }
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