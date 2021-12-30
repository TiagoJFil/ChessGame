package chess

import Board
import androidx.compose.ui.window.application
import chess.Storage.ChessDataBase
import chess.UI.Compose.App
import chess.domain.Player
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo
import isel.leic.tds.storage.mongodb.createMongoClient


fun main() = application {
    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()

    val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE )
    this.App(chessGame,driver)

}