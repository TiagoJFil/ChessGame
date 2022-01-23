package domain

import Board
import chess.domain.*
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
import org.junit.Assert.*
import org.junit.Test

class PieceMoveTest {
    @Test
    fun `PieceMove toString`() {
        val pmove = PieceMove(Square(Column.D, Row.One), Square(Column.D, Row.Three))
        val sut = Board()
        assertEquals("Qd1d3", pmove.formatToString(sut))

    }

    @Test
    fun `String to PieceMove`() {
        assertThrows(IllegalArgumentException::class.java) {
            "d1d3".formatToPieceMove()
        }
        val pmove = "Pd1d3".formatToPieceMove()
        assertEquals(pmove, PieceMove(Square(Column.D, Row.One), Square(Column.D, Row.Three)))
    }

}

class MoveDataClassTest {
    @Test
    fun `Move toString`() {
        val string = "Pa2a4"
        val move = Move(string)
        assertEquals(string,move.toString())
    }

    @Test
    fun `String to Move`() {
        val string = "Pa2a4"
        val move = Move(string)
        assertEquals(string,move.toString())
    }

    @Test
    fun `String  with extras to Move `() {
        assertThrows(IllegalArgumentException::class.java) {
            val string = "Pa2xa4=Q"
            val sut = Board()
            val move = string.toMove(sut)
        }
    }

    @Test
    fun `String without piece to Move`() {
        val string = "a2a5"
        val sut = Board()
        val move =  string.toMove(sut)
        assertEquals("Pa2a5",move.toString())
    }

    @Test
    fun `String not valid to Move`() {
        assertThrows(IllegalArgumentException::class.java) {
            val string = "Pa2a5a"
            val sut = Board()
            string.toMove(sut)
        }
    }

    @Test
    fun `Move given is promotable`(){
        val sut = Board()
            .makeMove("Ph2h4").makeMove("pg7g5")
            .makeMove("Ph4g5").makeMove("ng8h6")
            .makeMove("Pg5g6").makeMove("nh6g4")
            .makeMove("Pg6g7").makeMove("ng4h2")
        val move = "pg7g8"
        assertTrue(sut.isTheMovementPromotable(move))
    }

    @Test
    fun `Move given with piece is formatted`(){
        val sut = Board()
        val move = "pa2a4"
        assertEquals(move.toMove(sut),Move("pa2a4"))
    }

    @Test
    fun `Testing move given without piece`(){
        val sut = Board()
        val move = "a2a4"
        assertEquals(move.toMove(sut),Move("Pa2a4"))
    }

    @Test
    fun `Testing move given without piece2`(){
        val sut = Board()
        val move = "a3a4"
        assertNull(move.toMove(sut))
    }

    @Test
    fun `Move given throws exception`(){
        val move = "a2Pa4"
        assertThrows(IllegalArgumentException::class.java){
            Move(move)
        }
    }

    @Test
    fun `Move given throws exception2`(){
        val move = "a2pa4"
        assertThrows(IllegalArgumentException::class.java){
            move.formatToPieceMove()
        }
    }

    @Test
    fun `Formmating right given move`(){
        val startSquare = Square(column = Column.A,row = Row.Two)
        val endSquare = Square(column = Column.A,row = Row.Four)
        val move = "Pa2a4"
        val format = move.formatToPieceMove()
        assertEquals(PieceMove(startSquare = startSquare, endSquare = endSquare),format)
    }

    @Test
    fun `Piece move to string`(){
        val sut = Board()
        val move = "Pa2a4"
        val format = move.formatToPieceMove()
        val stringMove = format.formatToString(sut)
        assertEquals(move,stringMove)
    }

    @Test
    fun `Getting moves from a null piece returns empty list`(){
        val sut = Board()
        val square = Square(column = Column.E, row = Row.Three)
        val knightDirections = listOf(
            Direction(RIGHT,0),
            Direction(LEFT ,0),
            Direction(0    , DOWN),
            Direction(0    , UP),
            Direction(RIGHT, DOWN),
            Direction(RIGHT, UP),
            Direction(LEFT , DOWN),
            Direction(LEFT , UP)
        )
        val moves = getMovesByAddingDirection(knightDirections,square,sut)
        assertEquals(moves, emptyList<PieceMove>())
    }

    @Test
    fun `getting all moves from King while theres no space to move returns empty list`(){
        val sut = Board()
        val square = Square(column = Column.E, row = Row.One)
        val knightDirections = listOf(
            Direction(RIGHT,0),
            Direction(LEFT ,0),
            Direction(0    , DOWN),
            Direction(0    , UP),
            Direction(RIGHT, DOWN),
            Direction(RIGHT, UP),
            Direction(LEFT , DOWN),
            Direction(LEFT , UP)
        )
        val moves = getMovesByAddingDirection(knightDirections,square,sut)
        assertEquals(moves, emptyList<PieceMove>())
    }

    @Test
    fun `Getting moves from king with space to move returns list of correct moves`(){
        val sut = Board().makeMove("Pe2e4").makeMove("pe7e6")
            .makeMove("Pf2f4").makeMove("pe6e5")
        val square = Square(column = Column.E, row = Row.One)
        val knightDirections = listOf(
            Direction(RIGHT,0),
            Direction(LEFT ,0),
            Direction(0    , DOWN),
            Direction(0    , UP),
            Direction(RIGHT, DOWN),
            Direction(RIGHT, UP),
            Direction(LEFT , DOWN),
            Direction(LEFT , UP)
        )
        val moves = getMovesByAddingDirection(knightDirections,square,sut)
        val listOfMoves = listOf(
            PieceMove(startSquare = Square(column = Column.E, row = Row.One),
                endSquare = Square(column = Column.E, row = Row.Two)),
            PieceMove(startSquare = Square(column = Column.E, row = Row.One),
                endSquare = Square(column = Column.F, row = Row.Two)))
        assertEquals(moves, listOfMoves)
    }

    @Test
    fun `Queen can't move with pieces infront`(){

    }
}
