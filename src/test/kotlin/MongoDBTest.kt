/*
class MongoDBTest(){
    @Test(expected = Exception::class)
    suspend fun `accessing game with id having spaces or being blank throws`(){
        val dbInfo = getDBConnectionInfo()
        val driver =
            if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
            else createMongoClient()
        val game = ChessRepository(driver.getDatabase(dbInfo.dbName)).doesGameExist(GameName(""))
    }
}
*/