package chess.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import chess.Chess
import chess.GameName
import chess.storage.getMovesAsString
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.domain.getPiecePossibleMovesFrom
import chess.domain.isTheMovementPromotable
import doesBelongTo
import doesNotBelongTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Represents the status of the game.
 */
sealed class GameStatus

/**
 * The game is in progress.
 * @property infoToView     the special information to be displayed to the user, may be null(no special information).
 */
class GameStarted(val infoToView: ShowInfo?) : GameStatus()

/**
 * The game has not started yet.
 */
object GameNotStarted : GameStatus()

/**
 * The game has ended.
 * @property infoToView    the special information to be displayed to the user
 */
class GameOver(val infoToView: ShowInfo) : GameStatus()

/**
 * Represents the game view main properties
 */
class GameContentViews(
    private val chessInfo: Chess,
    private val action: GameActions,
    private val Cscope : CoroutineScope
) {
    private val chess = mutableStateOf(chessInfo)
    private val gameStatus : MutableState<GameStatus> = mutableStateOf(GameNotStarted)
    private val promotionValue = mutableStateOf("")                        // The type of piece the user wants to promote to
    private val canSelectAPromotion = mutableStateOf(true)                 // Oppener of the dialog to select a promotion
    val actionToEnterGame: MutableState<ACTION?> = mutableStateOf(null)    //Oppener for the select game name dialog
    private val clicked: MutableState<Clicked> = mutableStateOf(NONE)            // The state of click on a tile
    private val showPossibleMoves = mutableStateOf(true)                    // Show possible moves starts as true by default
    private val possibleMovesList = mutableStateOf(emptyList<Square>())          // List of possible moves for a piece

    private val movesToDisplay = mutableStateOf("")
    private val move = mutableStateOf("")

    fun getChess() = chess.value
    fun getGameStatus(): GameStatus = gameStatus.value
    fun getMoveToDisplay() = movesToDisplay.value

    fun updatePossibleMovesOption(value: Boolean) {
        showPossibleMoves.value = value
    }

    fun openAGame(gameId: GameName) {
        Cscope.launch {
            updateGame(action.openGame(gameId, chess.value))
        }
    }

    fun joinAGame(gameId: GameName) {
        Cscope.launch {
            updateGame(action.joinGame(gameId, chess.value))
        }
    }

    private fun playAGame(move: String) {
        Cscope.launch {
            updateGame(action.play(move, chess.value))
        }
    }

    fun refreshGame(scope: CoroutineScope) {
        scope.launch {
            updateGame(action.refreshBoard(chess.value))
        }
    }

    /**
     * Updates the [promotionValue] with the given value to add to the move string
     */
    private fun updatePromotionValue(value: String) {
        promotionValue.value = "=$value"
    }

    /**
     * Clears the possibleMoveList if the option to showPossibleMoves is disabled
     */
    fun clearPossibleMovesListIfOptionDisabled() {
        if(!showPossibleMoves.value)
            possibleMovesList.value = emptyList()
    }

    /**
     * Verifies if the move square is a possible move for other piece
     */
    fun isTileAPossibleMove(square: Square) =
        showPossibleMoves.value && possibleMovesList.value.contains(square)

    /**
     * Verify if the [Square] given corresponds to a selected tile by the user
     */
    fun isTileSelected(square: Square) =
        clicked.value is START && (clicked.value as START).square == square.toString()

    /**
     * This function is called when the user clicks on a tile
     * @param square the square that the user clicked on
     */
    fun handleClick(square: Square){
        clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
    }

    /**
     * This function analyzes the [clicked] state and acts accordingly
     */
    @Composable
    fun actOnClickValue(){
        when(clicked.value){
            is START -> {
                val start = clicked.value as START
                val board = chess.value.board
                val startSquare = start.square.toSquare()
                val currentPlayer = chess.value.localPlayer

                if (currentPlayer != board.player) {
                    clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                    clicked.value = NONE
                } else {
                    if (startSquare.doesNotBelongTo(currentPlayer, board)) {
                        clicked.value = NONE
                    } else {
                        move.value = start.square
                        if(showPossibleMoves.value) {
                            val moves = move.value.toSquare().getPiecePossibleMovesFrom(board,currentPlayer)
                            if (moves.isNotEmpty()) {
                                val possibleMoves = moves.map { it.endSquare }
                                possibleMovesList.value = possibleMoves
                            }
                        }
                    }
                }
            }
            is FINISH -> {
                val finish = clicked.value as FINISH
                val finishSquare = finish.square.toSquare()
                val startSquare = move.value.toSquare()
                val player = chess.value.localPlayer
                require(player != null)
                if(promotionValue.value == "" && chess.value.board.isTheMovementPromotable("$startSquare$finishSquare") && canSelectAPromotion.value) {
                    selectPossiblePromotions(
                        player,
                        onClose = { canSelectAPromotion.value = false }
                    ) {   piece ->
                        updatePromotionValue(piece)
                        canSelectAPromotion.value = false
                    }
                }else {
                    val finalMoveString = move.value + finish.square + promotionValue.value
                    playAGame(finalMoveString)
                    promotionValue.value = ""
                    canSelectAPromotion.value = true
                }
            }
        }
    }
    /**
     * Updates the [chess] state and view variables depending of the given [Result]
     * @param result        the result of the action
     */
    private fun updateGame(result: Result) {
        when (result) {
            is OK -> {
                chess.value = result.chess
                val gameId = result.chess.currentGameId
                require(gameId != null)
                if (result.moves != null)
                    movesToDisplay.value = result.moves.getMovesAsString()

                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

                gameStatus.value = GameStarted(null)
            }
            is CHECK -> {
                val player = result.playerInCheck
                chess.value = result.chess
                movesToDisplay.value = result.moves.getMovesAsString()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

                gameStatus.value =  GameStarted(ShowCheck(player))
            }
            is CHECKMATE -> {
                val player = result.playerInCheckMate
                chess.value = result.chess
                movesToDisplay.value = result.moves.getMovesAsString()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

                gameStatus.value =  GameOver(ShowCheckmate(player) )
            }
            is STALEMATE -> {
                chess.value = result.chess
                movesToDisplay.value = result.moves.getMovesAsString()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

                gameStatus.value = GameOver(ShowStalemate)

            }
            is EMPTY -> {
                if (clicked.value is FINISH) {
                    val finish = clicked.value as FINISH
                    val finishSquare = finish.square.toSquare()
                    val startSquare = move.value.toSquare()
                    val currentPlayer = chess.value.localPlayer
                    val endPiece = chess.value.board.getPiece(finish.square.toSquare())
                    require((currentPlayer != null))
                    when {
                        //if its the same square a enemy piece
                        startSquare == finishSquare || endPiece != null && endPiece.player != currentPlayer-> {
                            clicked.value = NONE
                        }

                        //if its a friendly piece
                        finishSquare.doesBelongTo(currentPlayer, chess.value.board) -> {
                            clicked.value = START(finish.square)
                        }
                        !possibleMovesList.value.contains(finishSquare) -> {
                            clicked.value = START(startSquare.toString())
                        }
                        else -> {
                            clicked.value = NONE
                        }
                    }
                    clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                }
            }
        }
    }
}

private fun clearPossibleMovesIfOptionEnabled(showPossibleMoves: Boolean, selected: MutableState<List<Square>>){
    if(showPossibleMoves) selected.value = emptyList()
}