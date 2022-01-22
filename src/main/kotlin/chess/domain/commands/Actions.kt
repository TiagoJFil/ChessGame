package chess.domain.commands

import chess.Chess
import chess.GameName

interface Actions {

    /**
     * Function to open a game as a player with the color WHITE and the game name received.
     */
    suspend fun openGame(gameId: GameName, chess: Chess): Result

    /**
     * Function to join a game as a player with the color BLACK and the game name received.
     */
    suspend fun joinGame(gameId: GameName, chess: Chess): Result

    /**
     * Function to move a piece on the board and update the database with the new move.
     */
    suspend fun play(move: String, chess: Chess): Result

    /**
     * Function to update the board with the last move played.
     */
    suspend fun refreshBoard(chess: Chess): Result

}