package chess

import Board
import androidx.compose.ui.window.application
import chess.storage.ChessRepository
import chess.storage.MongoDb.createMongoClient
import chess.ui.App
import chess.storage.DbMode
import chess.storage.getDBConnectionInfo



fun main() {
    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()


    driver.use {//TODO maybe remove play.white and put null
        val chessGame = Chess( Board(), ChessRepository(driver.getDatabase(dbInfo.dbName)), null, null )
        application(exitProcessOnExit = false) {
            App(chessGame)
        }

    }

}