package chess.domain.commands

import chess.Chess
import chess.GameName
import chess.Storage.Move
import chess.domain.Player

interface ActionInterface {

    /**
     * Function to open a game as a player with the color WHITE and the game name received.
     */
    fun openGame(gameId: GameName, chess: Chess): Result

    /**
     * Function to join a game as a player with the color BLACK and the game name received.
     */
    fun joinGame(gameId: GameName,chess: Chess): Result

    /**
     * Function to move a piece on the board and update the database with the new move.
     */
    fun play(move: String, chess: Chess): Result

    /**
     * Function to get update the board with the last move played.
     */
    fun refreshBoard(chess: Chess, movesList: Iterable<Move>?): Result

    //TODO("CHOOSE IF WE ARE USING A ACTION CLASS OR NOT")
}


