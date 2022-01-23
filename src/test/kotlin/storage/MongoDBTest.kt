package storage

import chess.GameName
import chess.storage.ChessRepository
import chess.storage.DatabaseMove
import chess.storage.DbMode
import chess.storage.MongoDb.createMongoClient
import chess.storage.getDBConnectionInfo
import com.mongodb.client.MongoClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before



class MongoDBTest(){
    private fun getMongoClient(): MongoClient {
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()

        return driver
    }


    @Before
    fun prepareTest() {
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()

        val database = driver.getDatabase(dbInfo.dbName)
        database.getCollection("Test").drop()
    }

    @Test
    fun `verifying if a game that doesnt exist exists`() {
        val dbInfo = getDBConnectionInfo()
        val id = "empty"
        runBlocking {

            getMongoClient().use { driver ->

                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")
                val game = repo.doesGameExist(GameName(id))
                assertEquals(game, false)
            }
        }
    }

    @Test
    fun `create a new document if it doesnt exist already`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new"
        runBlocking {

            getMongoClient().use { driver ->
                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")


                    val game = repo.createGameDocumentIfItNotExists(GameName(id))
                    assertEquals(game, true)
                val gameRes = repo.doesGameExist(GameName(id))
                assertEquals(gameRes, true)
            }
        }
    }

    @Test
    fun `trying to create a document that already exists doesnt work`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new"
        runBlocking {

            getMongoClient().use { driver ->
                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")
                repo.createGameDocumentIfItNotExists(GameName(id))
                val gameRes = repo.doesGameExist(GameName(id))
                assertEquals(gameRes, true)
                val game = repo.createGameDocumentIfItNotExists(GameName(id))
                assertEquals(game, false)

            }
        }
    }


    @Test
    fun `Adds a move to an existing game`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new1"
        runBlocking {

            getMongoClient().use { driver ->

                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")

                repo.createGameDocumentIfItNotExists(GameName(id))

                val move = DatabaseMove("Pe2e4")
                val result = repo.addMoveToDb(move, GameName(id))
                assertEquals(result, true)


            }
        }
    }

    @Test
    fun `trying to add a move to a non existing game doesnt work`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new1"
        runBlocking {

            getMongoClient().use { driver ->

                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")

                val move = DatabaseMove("Pe2e4")
                val result = repo.addMoveToDb(move, GameName(id))
                assertEquals(result, false)
            }
        }
    }

    @Test
    fun `trying to get last move from a non existing game returns null`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new1"
        runBlocking {

            getMongoClient().use { driver ->

                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")
                val result = repo.getLastMove(GameName(id))
                assertNull(result)
            }
        }
    }

    @Test
    fun `verify if a move was added to a game`() {
        val dbInfo = getDBConnectionInfo()
        val id = "new12"
        runBlocking {

            getMongoClient().use { driver ->

                val repo = ChessRepository(driver.getDatabase(dbInfo.dbName), "Test")

                val game = repo.createGameDocumentIfItNotExists(GameName(id))
                val move = "Pe2e4"
                val result = repo.addMoveToDb(DatabaseMove(move), GameName(id))

                val movesCount = repo.getMoveCount(GameName(id))
                assertEquals(movesCount, 1)

                val moves = repo.getAllMoves(GameName(id))
                assertEquals(moves.toList()[0].move,move)

            }
        }
    }

}
