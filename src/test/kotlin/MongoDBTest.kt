
import chess.GameName
import chess.Storage.ChessRepository
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo
import isel.leic.tds.storage.mongodb.createMongoClient
import org.junit.Test

class MongoDBTest(){
    @Test(expected = Exception::class)
    fun `accessing game with id having spaces or being blank throws`(){
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()
        val game = ChessRepository(driver.getDatabase(dbInfo.dbName)).doesGameExist(GameName(""))
    }
}