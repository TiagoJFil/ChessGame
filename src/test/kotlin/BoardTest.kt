import chess.domain.Player
import chess.domain.board_components.Column
import chess.domain.board_components.Row
import chess.domain.board_components.Square
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
            sut.getKingSquare(Player.BLACK)
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
    fun `Pawn promotion to Queen and Bishop`(){
        val sut = Board().makeMove("Ph2h4").makeMove("pa7a5")
            .makeMove("ph4h5").makeMove("pa5a4")
            .makeMove("ph5h6").makeMove("pa4a3")
            .makeMove("ph6g7").makeMove("pa3b2")

        val whitePromotionSquare = Square(column = Column.H, row = Row.Eight)
        val blackPromotionSquare = Square(column = Column.A, row = Row.One)

        val promotionWhite = sut.moveAndPromotePiece("pg7h8",'Q')

        val promotionBlack = sut.moveAndPromotePiece("pb2a1",'b')

        val piece = promotionWhite.getPiece(whitePromotionSquare)
        assertEquals(piece,Queen(player = Player.WHITE))

        val piece2 = promotionBlack.getPiece(blackPromotionSquare)
        assertEquals(piece2,Bishop(player = Player.BLACK))
    }

    @Test
    fun `Pawn promotion to Knight and Rook`(){
        val sut = Board().makeMove("Ph2h4").makeMove("pa7a5")
            .makeMove("ph4h5").makeMove("pa5a4")
            .makeMove("ph5h6").makeMove("pa4a3")
            .makeMove("ph6g7").makeMove("pa3b2")

        val whitePromotionSquare = Square(column = Column.H, row = Row.Eight)
        val blackPromotionSquare = Square(column = Column.A, row = Row.One)

        val promotionWhite = sut.moveAndPromotePiece("pg7h8",'N')

        val promotionBlack = sut.moveAndPromotePiece("pb2a1",'r')

        val piece = promotionWhite.getPiece(whitePromotionSquare)
        assertEquals(piece,Knight(player = Player.WHITE))

        val piece2 = promotionBlack.getPiece(blackPromotionSquare)
        assertEquals(piece2,Rook(player = Player.BLACK))
    }

    @Test
    fun `Pawn promotion to a invalid piece`(){
        val sut = Board().makeMove("Ph2h4").makeMove("pa7a5")
            .makeMove("ph4h5").makeMove("pa5a4")
            .makeMove("ph5h6").makeMove("pa4a3")
            .makeMove("ph6g7").makeMove("pa3b2")

        assertThrows(IllegalArgumentException::class.java) {
            val promotionInvalid = sut.moveAndPromotePiece("pg7h8", 'T')
        }



    }

    @Test
    fun `board toString()` (){
        val sut = Board()
        val boardString = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR"
        assertEquals(boardString, sut.toString() )
    }

    /*
    @Test
    fun `get board as a list`(){
        val sut = Board()
        val bList = sut.asList().map { it.toString() }
        val expected = listOf(
            "r","n","b","q","k","b","n","r",
            "p","p","p","p","p","p","p","p",
            "null","null","null","null","null","null","null","null",
            "null","null","null","null","null","null","null","null",
            "null","null","null","null","null","null","null","null",
            "null","null","null","null","null","null","null","null",
            "P","P","P","P","P","P","P","P",
            "R","N","B","Q","K","B","N","R"
        )
        assertEquals(expected, bList)
    }
    */


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
