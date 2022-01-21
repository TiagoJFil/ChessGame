import chess.Chess
import chess.GameName
import chess.domain.*
import chess.domain.commands.*
import chess.storage.ChessRepository
import chess.storage.DbMode
import chess.storage.MongoDb.createMongoClient
import chess.storage.getDBConnectionInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ActionsTest {
    private val dbInfo = getDBConnectionInfo()
    private val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()
    private val db =  ChessRepository(driver.getDatabase(dbInfo.dbName),"Test")
    private val actions = GameActions()

    private suspend fun makeMoves(moves: List<String>, id : String): Chess {
        var Wchess = Chess(Board(), db, null, Player.WHITE)
        var Bchess = Chess(Board(), db, null, Player.BLACK)
        var Wres : Result = actions.openGame(GameName(id), Wchess)
        var Bres : Result = actions.joinGame(GameName(id), Bchess)
        require(Bres is OK)
        require(Wres is OK)
        Wchess = Wres.chess
        Bchess = Bres.chess
        var count = 0
        moves.forEach {
            if(count % 2 == 0) {
                if(count != 0) {
                    val wchess = actions.refreshBoard(Wchess)
                    when(wchess){
                        is OK -> Wchess = wchess.chess
                        is CHECK -> Wchess = wchess.chess
                    }
                }
                Wres = actions.play(it, Wchess)
                when(Wres){
                    is OK -> Wchess = (Wres as OK).chess
                    is CHECK -> Wchess = (Wres as CHECK).chess
                }
            }
            else{
                val bchess = actions.refreshBoard(Bchess)
                when(bchess){
                    is OK -> Bchess = bchess.chess
                    is CHECK -> Bchess = bchess.chess
                }

                Bres = actions.play(it, Bchess)
                when(Bres){
                    is OK -> Bchess = (Bres as OK).chess
                    is CHECK -> Bchess = (Bres as CHECK).chess
                }
            }
            count++
        }
        if(count % 2 == 0) {
            val chess = actions.refreshBoard(Wchess)
            when(chess){
                is OK -> return chess.chess
                is CHECK -> return chess.chess
            }

        }
        else {
            val chess = actions.refreshBoard(Bchess)
            when(chess){
                is OK -> return chess.chess
                is CHECK -> return chess.chess
            }
        }
            return Chess(Board(), db, null, Player.WHITE)
    }


    @Before
    fun prepareTest() {
        val database = driver.getDatabase(dbInfo.dbName)
        database.getCollection("Test").drop()
    }

    @Test
    fun `open a game`() {
        val chess = Chess(Board(), db, null, Player.WHITE)
        runBlocking {
            val res = actions.openGame(GameName("testGame"), chess)

            assertTrue(res is OK)
            require(res is OK)
            assertEquals(
                "rnbqkbnr" +
                        "pppppppp" +
                        "        ".repeat(4) +
                        "PPPPPPPP" +
                        "RNBQKBNR", res.chess.board.toString()
            )
        }
    }

    @Test
    fun `try to join a game that has not been openned yet`(){
        val chess = Chess(Board(), db, null, Player.WHITE)
        runBlocking {
            val res = actions.joinGame(GameName("abcds"), chess)
            assertTrue(res is EMPTY )
        }
    }

    @Test
    fun `try to join a game that has been opened`(){
        val chess = Chess(Board(), db, null, Player.WHITE)
        runBlocking {
            actions.openGame(GameName("testGame"), chess)
            val res = actions.joinGame(GameName("testGame"), chess)
            assertTrue(res is OK)
            require(res is OK)
            assertEquals(
                "rnbqkbnr" +
                        "pppppppp" +
                        "        ".repeat(4) +
                        "PPPPPPPP" +
                        "RNBQKBNR", res.chess.board.toString()
            )
        }
    }


    @Test
    fun `make a play on a opened game`(){
        val chess = Chess(Board(), db, null, Player.WHITE)
        runBlocking {
            val openRes =actions.openGame(GameName("testGame"), chess)
            require(openRes is OK)
            val res = actions.play("a2a4", openRes.chess)
            assertTrue(res is OK)
            require(res is OK)
            assertEquals(
                "rnbqkbnr" +
                        "pppppppp" +
                        "        " +
                        "        " +
                        "P       " +
                        "        " +
                        " PPPPPPP" +
                        "RNBQKBNR", res.chess.board.toString()
            )
        }
    }

    @Test
    fun `join a already opened game`(){
        val chess = Chess(Board(), db, null, Player.WHITE)
        runBlocking {
            val openRes =actions.openGame(GameName("testGame1"), chess)
            require(openRes is OK)
            val res = actions.play("a2a4", openRes.chess)
            require(res is OK)
            val joinRes = actions.joinGame(GameName("testGame1"), res.chess)
            assertTrue(joinRes is OK)
            require(joinRes is OK)
            assertEquals(
                "rnbqkbnr" +
                        "pppppppp" +
                        "        " +
                        "        " +
                        "P       " +
                        "        " +
                        " PPPPPPP" +
                        "RNBQKBNR", joinRes.chess.board.toString()
            )
        }
    }
    @Test
    fun `Verify if detects checkmate`(){
        val moves = listOf(
            "f2f3","e7e5",
            "g2g4"
        )
        runBlocking {
            val board = makeMoves(moves, "testGame2")
            val res = actions.play("d8h4", board)
            assertEquals(res is CHECKMATE, true)
            require(res is CHECKMATE)
            assertEquals(
                "rnb kbnr" +
                        "pppp ppp" +
                        "        " +
                        "    p   " +
                        "      Pq" +
                        "     P  " +
                        "PPPPP  P" +
                        "RNBQKBNR", res.chess.board.toString()
            )
        }
    }

    @Test
    fun `Verify if detects check`(){
        val moves = listOf(
            "f2f3","e7e5",
            "d2d4"
        )
        runBlocking {
            val board = makeMoves(moves, "testGame3")
            val res = actions.play("d8h4", board)
            assertEquals(res is CHECK, true)
        }
    }

    @Test
    fun `Verify if detects stalemate`(){
        val moves = listOf(
            "Pe2e3","pa7a5",
            "Qd1h5","Ra8a6",
            "Qh5a5","ph7h5",
            "ph2h4","ra6h6",
            "Qa5c7","pf7f6",
            "qc7d7","ke8f7",
            "qd7b7","qd8d3",
            "qb7b8","qd3h7",
            "Qb8c8","Kf7g6")
        runBlocking {
            val board = makeMoves(moves, "testGame4")
            val res = actions.play("c8e6", board)
            assertEquals(res is STALEMATE, true)
        }
    }

    @Test
    fun `Make en passant ,castle `(){
        val moves = listOf(
            "Pe2e4","ph7h6",
            "Pe4e5","pf7f6",
            "Ph2h3","pd7d5",
            "e5e6","f1e2",
            "c7c6","g1f3",
            "g7g6"
        )
        runBlocking {
            val board = makeMoves(moves, "testGame5")
            val res = actions.play("e1g1", board)
            assertEquals(res is OK, true)
        }
    }

    @Test
    fun `Detects illegal move`(){
        val moves = listOf(
            "f2f3","e7e5",
            "d2d4")

        runBlocking {
            val board = makeMoves(moves, "testGame3")
            val res = actions.play("d8a2", board)
            assertEquals(res is EMPTY, true)
        }
    }

    @Test
    fun `Make promotion`(){
        val moves = listOf(
            "f2f4","e7e5",
            "f4f5","d7d6",
            "f5f6","d8d7",
            "f6g7","d6d5",
        )
        runBlocking {
            val board = makeMoves(moves, "testGame6")
            val res = actions.play("g7h8=Q", board)
            assertEquals(res is OK, true)
        }
    }


}
