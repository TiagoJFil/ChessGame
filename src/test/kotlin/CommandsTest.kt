class ActionsTest {
/*
    @Test
    fun `filterInput works when the move is something like a2a8=Q`(){
        val input = "a2xa8=Q"
        val expected = "Pa2a8"
        val databaseexpected = "Pa2xa8=Q"
        val actual = filterInput(input,Board())
        assertEquals(expected, actual!!.filteredMove)
        assertEquals(databaseexpected, actual!!.databaseMove)
    }
    */

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