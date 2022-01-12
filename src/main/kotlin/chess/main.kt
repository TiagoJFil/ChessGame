package chess

import Board
import androidx.compose.ui.window.application
import chess.Storage.ChessRepository
import chess.Storage.MongoDb.createMongoClient
import chess.UI.Compose.App
import chess.domain.Player
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo



fun main() {
    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()


    driver.use {
        val chessGame = Chess(Board(), ChessRepository(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE )
        application {
            App(chessGame)
        }

    }

}