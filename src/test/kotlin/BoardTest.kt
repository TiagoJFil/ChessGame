import chess.domain.Player
import chess.domain.board_components.toSquare
import org.junit.Assert.*
import org.junit.Test

class BoardTest {

    @Test
    fun `Initial position Board`() {
        val sut = Board()
        assertEquals(
            "rnbqkbnr"+
                    "pppppppp"+
                    "        ".repeat(4) +
                    "PPPPPPPP"+
                    "RNBQKBNR", sut.toString() )
    }
    @Test
    fun `MakeMove in Board`() {
        val sut = Board().makeMove("Pe2e4").makeMove("Pe7e5").makeMove("Nb1c3")
        assertEquals(
            "rnbqkbnr"+
                    "pppp ppp"+
                    "        "+
                    "    p   "+
                    "    P   "+
                    "  N     "+
                    "PPPP PPP"+
                    "R BQKBNR", sut.toString() )
    }

    @Test
    fun `getPieceAt e1 returns the king`(){
        val sut = Board()
        val square = "e1".toSquare()
        assertEquals ("K", sut.getPiece(square).toString())
    }

    @Test
    fun `getPieceAt at an empty square null`(){
        val sut = Board()
        val square = "e5".toSquare()
        assertNull(sut.getPiece(square))
    }

    @Test
    fun `getKingSquare for white returns the king square`(){
        val sut = Board()
        val king = sut.getKingSquare(sut.player)
        assertEquals ("e1".toSquare(), king)
    }

    @Test
    fun `getKingSquare for black returns the king square`(){
        val sut = Board()
        val king = sut.getKingSquare(!sut.player)
        assertEquals ("e8".toSquare(), king)
    }

    @Test
    fun `getKingSquare for white without a king throws error`(){
        assertThrows(IllegalStateException::class.java) {
            val sut = Board().makeMove("Pa2e8")
            val king = sut.getKingSquare(Player.BLACK)
        }
    }


    @Test
    fun `verify Square doesBelong function` (){
        val sut = Board()
        val square = "e1".toSquare()
        val squareEmpty = "e4".toSquare()
        assertEquals(true, square.doesBelongTo(Player.WHITE,sut ) )
        assertEquals(false, squareEmpty.doesBelongTo(Player.BLACK,sut ) )
    }


    @Test
    fun `verify getPieceAt function` (){
        val sut = Board()

    }

    @Test
    fun `toString()` (){
        val sut = Board()
        val boardString = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR"
        assertEquals(boardString, sut.toString() )
    }



/*
    @Test
    fun `empty board equals empty board`(){
        val a = Board()
        val b = Board()
        assertEquals (Board(),Board())
        val Boar = Board()
    }
*/

}
