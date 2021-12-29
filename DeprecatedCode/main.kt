package chess.DeprecatedCode

import Board
import chess.Chess
import chess.Storage.ChessGameAccessException
import chess.Storage.ChessDataBase
import chess.Storage.Move
import chess.buildCommandHandler
import chess.domain.Player
import chess.domain.commands.CONTINUE
import chess.domain.commands.ERROR
import chess.domain.commands.EXIT
import chess.domain.commands.getNewChess
import displayView
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo
import isel.leic.tds.storage.mongodb.createMongoClient
import readCommand

fun main(args: Array<String>) {

    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()




    try {
        var chessGame = Chess(Board(),ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE )
        val handler = buildCommandHandler(chessGame)
        while (true) {

            val (command, parameter) = readCommand(chessGame)
            val action = handler[command]
            if (action == null) println("Invalid command")
            else {
                when (val result = action.command(parameter)) {
                is EXIT -> { action.display(result.message) ; break }
                is CONTINUE<*> -> { action.display(result.data)  ; chessGame = getNewChess(chessGame, result.data) }
                is ERROR -> {
                    displayView(result.message)
                }
                }

            }
        }
    }catch (e: ChessGameAccessException){
        println("An unknown error occurred while trying to reach the database. " +
                if (dbInfo.mode == DbMode.REMOTE) "Check your network connection."
                else "Is your local database started?")
    }finally {
        println("Closing driver ...")
        driver.close()
    }

}
