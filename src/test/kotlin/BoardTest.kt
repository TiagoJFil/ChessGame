import chess.domain.Player
import chess.domain.board_components.toSquare
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertSame
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
        assertEquals ("K", sut.getPieceAt(square).toString())
    }

}
