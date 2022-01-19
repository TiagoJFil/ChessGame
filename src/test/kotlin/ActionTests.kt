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
        val moves = listOf<String>(
            "f2f3","e7e5","g2g4","d8h4"
        )
        val Whitechess = Chess(Board(), db, null, Player.WHITE)
        val Blackchess = Chess(Board(), db, null, Player.BLACK)

        runBlocking {
            val openRes = actions.openGame(GameName("testGame2"), Whitechess)
            val joinRes = actions.joinGame(GameName("testGame2"), Blackchess)
            require(openRes is OK)
            require(joinRes is OK)
            val open1 = actions.play(moves[0], openRes.chess)
            val refresh1 = actions.refreshBoard(joinRes.chess)
            require(refresh1 is OK)
            val join1 = actions.play(moves[1], refresh1.chess )
            require(open1 is OK)
            require(join1 is OK)
            val refresh2 = actions.refreshBoard(open1.chess)
            require(refresh2 is OK)
            val open2 = actions.play(moves[2], refresh2.chess)

            val refresh3 = actions.refreshBoard(join1.chess)
            require(refresh3 is OK)
            val join2 = actions.play(moves[3], refresh3.chess)


            assertEquals(join2 is CHECKMATE, true)
            require(join2 is CHECKMATE)
            assertEquals(
                "rnb kbnr" +
                        "pppp ppp" +
                        "        " +
                        "    p   " +
                        "      Pq" +
                        "     P  " +
                        "PPPPP  P" +
                        "RNBQKBNR", join2.chess.board.toString()
            )

        }


    }
}

/*
class CommandsTest {
    @Test
    fun `when joining or opening game null name returns an error of type ERROR`() {

        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()

        val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE)

        val handler = buildCommandHandler(chessGame)
        val openAction =
            handler["open"] //It will never be null cause it access open command, hence why we are using double bang in line below
        val openResult = openAction!!.command(null)
        val joinAction =
            handler["join"] //It will never be null cause it access join command, hence why we are using double bang in line below
        val joinResult = joinAction!!.command(null)
        if (openResult is ERROR && joinResult is ERROR) {
            assertEquals("ERROR: Missing game name.", openResult.message)
            assertEquals("ERROR: Missing game name.", joinResult.message)
        }
    }

    @Test
    fun `when joining a game that doesn't exists returns an error of type ERROR`() {
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()

        val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE)

        val handler = buildCommandHandler(chessGame)
        val joinAction =
            handler["join"] //It will never be null cause it access join command, hence why we are using double bang in line below
        val gameId = GameName("doesntExist")
        val joinResult = joinAction!!.command(gameId.id)


        if (joinResult is ERROR) {
            assertEquals("ERROR: Game ${gameId.id} does not exist.", joinResult.message)
        }
    }

    @Test
    fun `when refreshing or getting the moves played without a game opened returns an error of type ERROR`() {
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()

        val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE)
        val handler = buildCommandHandler(chessGame)
        val refreshAction =
            handler["refresh"] //It will never be null cause it access refresh command, hence why we are using double bang in line below
        val refreshResult = refreshAction!!.command(null)

        val movesAction =
            handler["moves"] //It will never be null cause it access moves command, hence why we are using double bang in line below
        val movesResult = movesAction!!.command(null)

        if (refreshResult is ERROR && movesResult is ERROR) {
            assertEquals("ERROR: Can't refresh without a game: try open or join commands.", refreshResult.message)
            assertEquals("No game, no moves.", movesResult.message)
        }
    }
}
/*
  @Test
  fun `refreshing when it's your turn to play returns an error of type ERROR`(){
      TODO()

     al dbInfo = getDBConnectionInfo()
      val driver =
          if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
          else createMongoClient()

      val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player(Colors.WHITE))

      val handler = buildCommandHandler(chessGame)
      val openAction = handler["open"] //It will never be null cause it access open command, hence why we are using double bang in line below
      val gameName = GameName("abre")
      val openResult = openAction!!.command(gameName.id)  //Open's has white pieces

      if(openResult is CONTINUE<*>){
          assertEquals("Game ${gameName.id} opened. Play with white pieces.", openResult.data)
      }

      val refreshAction = handler["refresh"] //It will never be null cause it access refresh command, hence why we are using double bang in line below
      val refreshResult = refreshAction!!.command(null)

      if(refreshResult is ERROR){
          assertEquals("ERROR: It's your turn: try play.", refreshResult.message)
      }
  }
      */