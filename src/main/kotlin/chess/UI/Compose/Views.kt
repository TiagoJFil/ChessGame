package chess.UI.Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.Chess
import chess.GameName
import chess.UI.Compose.board.*
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.domain.commands.Result
import chess.domain.getPiecePossibleMovesFrom
import doesBelongTo
import doesNotBelongTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


private const val ORANGE = 0xFFB5651E

val TILE_SIZE = 60.dp
private val MOVES_TEXT_SIZE_HEIGHT = 500.dp
private val MOVES_TEXT_SIZE_WIDTH = 200.dp
private val MOVES_FONT_SIZE = 16.sp
private val INFO_FONT_SIZE = 18.sp
private val WINDOW_HEIGHT = 600.dp
private val BACKGROUND_COLOR = Color(ORANGE)
private val MOVES_BACKGROUND_COLOR = Color.White

const val RESOURCE_PAWN_FILENAME = "pawn.png"
const val RESOURCE_ROOK_FILENAME = "rook.png"
const val RESOURCE_KNIGHT_FILENAME = "knight.png"
const val RESOURCE_BISHOP_FILENAME = "bishop.png"
const val RESOURCE_QUEEN_FILENAME = "queen.png"
const val RESOURCE_KING_FILENAME = "king.png"
private const val RESOURCE_ICON_FILENAME = "favicon.ico"


private enum class ACTION(val text: String) {
    OPEN("Open"),
    JOIN("Join")
}

private enum class GameStatus {
    GameStarted,
    GameNotStarted,
    GameOver;
}

/**
 * This is the main entry point for our application, as our app starts here.
 */
@Composable
fun ApplicationScope.App(chessInfo: Chess) {
    val chess = remember { mutableStateOf(chessInfo) }
    val promotionValue = remember { mutableStateOf("") }                     // The type of piece the user wants to promote to
    val isSelectingPromotion = remember { mutableStateOf(false) }           // Open the dialog to select a promotion
    val isAskingForName = remember { mutableStateOf(false) }                // Open the dialog to ask for a name
    val actionToDisplay = remember { mutableStateOf(ACTION.OPEN) }
    val clicked : MutableState<Clicked> = remember { mutableStateOf(NONE) }     // The state of click on a tile
    val showPossibleMoves = remember { mutableStateOf(true) }               // Show possible moves starts as true by default
    val move = remember { mutableStateOf("") }
    val possibleMovesList = remember { mutableStateOf(emptyList<Square>()) }      // List of possible moves for a piece
    val result : MutableState<Result> = remember { mutableStateOf(NONE()) }        // Result produced from making an action(moving, joining, etc)
    val infoToShow : MutableState<ShowInfo?> = remember { mutableStateOf(null) }
    val movesToDisplay = remember { mutableStateOf("") }
    val gameStatus = remember { mutableStateOf(GameStatus.GameNotStarted) }
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCE_ICON_FILENAME),
        title = "Chess",
        resizable = false
    ) {
        //while this function is running , there will be coroutines running
        val coroutineScope = rememberCoroutineScope()

        fun openAGame(gameId: GameName) {
            coroutineScope.launch {
                result.value = openGame(gameId, chess.value)
            }
        }
        fun joinAGame(gameId: GameName) {
            coroutineScope.launch {
                result.value = joinGame(gameId, chess.value)
            }
        }

        chessMenu(
            onClickOpen = {
            actionToDisplay.value = ACTION.OPEN; isAskingForName.value = true
            },
            onClickJoin = {
                actionToDisplay.value = ACTION.JOIN; isAskingForName.value = true
            },
            onClickShowMoves = {
                showPossibleMoves.value = it
            }
        )

        if (!showPossibleMoves.value) possibleMovesList.value = emptyList()

        if (isAskingForName.value) {
            getGameName(
                actionToDisplay.value.text,
                onClose = { isAskingForName.value = false },
                onSubmit = {
                if (actionToDisplay.value == ACTION.JOIN) {
                    joinAGame(it)
                } else {
                    openAGame(it)
                }
                    isAskingForName.value = false
                }
            )
        }

        if (isSelectingPromotion.value) {

            selectPossiblePromotions(
                chess.value.localPlayer,
                onClose = { isSelectingPromotion.value = false }
            ) {
                    promotionValue.value = "=$it"
                    isSelectingPromotion.value = false
                }
        }


        LaunchedEffect(chess.value) {
            while(true) {
                if(chess.value.currentGameId != null && chess.value.localPlayer != chess.value.board.player) {
                    result.value = refreshBoard(chess.value)
                }
                delay(1500)
            }
        }


    //TODO on open or join game chess board isnt uopdating after the game is deleted from db
        //above here is all confirmed
        //from down here the things must still be checked
        if (clicked.value is START) {
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
                        //TODO maybe improve getPiecePossibleMovesFrom
                        if (moves.isNotEmpty()) {
                            val possibleMoves = moves.map { it.endSquare }
                            possibleMovesList.value = possibleMoves
                        }
                    }
                    //getPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList, chess.value.board,chess.value.currentPlayer, move.value)
                }
            }

        }
//TODO QUANDO FAZEMOS PROMOTE E COMEMOS ALGUEM APARECE MAL NA DB
        // TODO QUANDO FAZEMOS CHECK E COMEMOS ALKGUEM APARECE MAL NA DB

        if (clicked.value is FINISH) {
            val finish = clicked.value as FINISH
            val finishSquare = finish.square.toSquare()
            val startSquare = move.value.toSquare()

            if(promotionValue.value == "" && chess.value.board.isTheMovementPromotable("$startSquare$finishSquare")) {
                isSelectingPromotion.value = true
            }else {
                val finalMoveString = move.value + finish.square + promotionValue.value

                coroutineScope.launch {
                    result.value = play(finalMoveString, chess.value)
                }

                promotionValue.value = ""
            }
        }


        handleResult(result, chess, infoToShow , showPossibleMoves, possibleMovesList,clicked,move.value, movesToDisplay,gameStatus)

        //TODO WHEN A LOT OF MOVES ARE PLAYED TO RIGHT MOVES  GO DOWN INTO INFINIY
//TODO stop the movement after the game is over


        when(gameStatus.value){
            GameStatus.GameNotStarted ->{
                MaterialTheme{
                    drawVisualsWithoutAStartedGame()
                }
            }
            GameStatus.GameStarted ->{
                MaterialTheme {
                    drawVisualsWithAStartedGame(chess.value,infoToShow.value,movesToDisplay.value,
                        checkIfIsAPossibleMove = { square ->
                            showPossibleMoves.value && possibleMovesList.value.contains(square)
                        },
                        checkIfTileIsSelected =   { square ->
                            clicked.value is START && (clicked.value as START).square == square.toString()
                        },
                        OnTileClicked = { square ->
                            clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
                        }
                    )
                }
            }
            GameStatus.GameOver ->{
                MaterialTheme {
                    drawVisualsWithAStartedGame(chess.value,infoToShow.value,movesToDisplay.value,
                        checkIfIsAPossibleMove = { false },
                        checkIfTileIsSelected =   { false },
                        OnTileClicked = { }
                    )
                }

            }
        }

    }
}



private fun handleResult(
    result: MutableState<Result>,
    chess: MutableState<Chess>,
    infoToShow: MutableState<ShowInfo?>,
    showPossibleMoves: MutableState<Boolean>,
    possibleMovesList: MutableState<List<Square>>,
    clicked: MutableState<Clicked>,
    move: String,
    movesToDisplay: MutableState<String>,
    gameStatus: MutableState<GameStatus>
) {

    when (result.value) {
        is OK -> {
            val res = (result.value as OK)
            chess.value = res.chess
            val gameId = res.chess.currentGameId
            require(gameId != null)
            if(res.moves != null)
                movesToDisplay.value = res.moves.getMovesAsString(gameId,chess.value.dataBase)

            result.value = NONE()
            infoToShow.value = null
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

            gameStatus.value = GameStatus.GameStarted
        }
        is CHECK -> {
            val res = (result.value as CHECK)
            val player = res.playerInCheck

            chess.value = res.chess
            result.value = NONE()
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

            infoToShow.value = showCheck(player)
        }
        is CHECKMATE -> {
            val res = (result.value as CHECKMATE)
            val player = res.playerInCheckMate

            chess.value = res.chess
            result.value = NONE()
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

            infoToShow.value = showCheckmate(player)
            gameStatus.value = GameStatus.GameOver
        }
        is STALEMATE -> {
            val res = (result.value as STALEMATE)

            chess.value = res.chess
            result.value = NONE()
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)

            infoToShow.value = showStalemate()
            gameStatus.value = GameStatus.GameOver
        }
        is chess.domain.commands.NONE  -> {
            if(clicked.value is FINISH) {
                val finish = clicked.value as FINISH
                val finishSquare = finish.square.toSquare()
                val startSquare = move.toSquare()
                val currentPlayer = chess.value.localPlayer
                val endPiece = chess.value.board.getPiece(finish.square.toSquare())


                when {
                    startSquare == finishSquare -> {
                        clicked.value = NONE
                        clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                    }
                    finishSquare.doesBelongTo(currentPlayer, chess.value.board) -> {
                        clicked.value = START(finish.square)

                        clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                    }
                    endPiece != null && endPiece.player != currentPlayer -> {
                        clicked.value = NONE

                        clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                    }
                    !possibleMovesList.value.contains(finishSquare) ->
                        clicked.value = START(startSquare.toString())
                    else -> {
                        clicked.value = NONE

                        clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                    }
                }

                result.value = NONE()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
            }
        }


    }

}



private suspend fun dealWithMovement(
    clicked: MutableState<Clicked>,
    possibleMovesList: MutableState<List<Square>>,
    showPossibleMoves: Boolean,
    chess: Chess,
    move: String,
    result: MutableState<Result>,
    promotionValue: MutableState<String>,
    isSelectingPromotion: MutableState<Boolean>
){
    val finish = clicked.value as FINISH
    val board = chess.board
    val finishSquare = finish.square.toSquare()
    val startSquare = move.toSquare()
    val currentPlayer = chess.localPlayer
    val endPiece = chess.board.getPiece(finish.square.toSquare())
    if(promotionValue.value == "" && board.isTheMovementPromotable("$startSquare$finishSquare")) {
        isSelectingPromotion.value = true
    }

    val finalMoveString = move + finish.square + promotionValue.value


    val value = play(finalMoveString, chess)

    result.value = value
    when {
        value is chess.domain.commands.NONE && startSquare == finishSquare -> {
            clicked.value = NONE
            clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
        }
        value is chess.domain.commands.NONE && finishSquare.doesBelongTo(currentPlayer, chess.board) -> {
            clicked.value = START(finish.square)

            clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
        }
        value is chess.domain.commands.NONE && endPiece != null && endPiece.player != currentPlayer -> {
            clicked.value = NONE

            clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
        }
        value is chess.domain.commands.NONE && !possibleMovesList.value.contains(finishSquare) ->
            clicked.value = START(startSquare.toString())
        else -> {
            clicked.value = NONE

            clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
        }
    }
    promotionValue.value = ""
}





@Composable
private fun drawVisualsWithAStartedGame(
    chess: Chess,
    infoToShow: ShowInfo?,
    movesPlayed: String,
    checkIfIsAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    OnTileClicked : (square: Square) -> Unit
) {
    val infoModifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp).background(Color.Red)
    val gameId = chess.currentGameId
    require(gameId != null) { "Game id is null" }

    Row(Modifier.background(BACKGROUND_COLOR).height(WINDOW_HEIGHT)) {

        drawCoordinateNumbers()

        Column{

            drawCoordinateLetters()

            Box {
                buildBackgroundBoard()
                boardToComposableView( chess.board, checkIfIsAPossibleMove, checkIfTileIsSelected, OnTileClicked)
            }

                val info = if (chess.board.player == chess.localPlayer) "Your turn" else "Waiting..."
                Text(
                    "Game:${gameId.id} | You:${chess.localPlayer} | $info",
                    fontSize = INFO_FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )

                when(infoToShow){
                    is showCheckmate-> {
                        val playerMessage = if (infoToShow.player == chess.localPlayer) "Lose." else "Win!!!"
                        Text(
                            "CHECKMATE You $playerMessage",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }
                    is showCheck -> {
                        if(infoToShow.player == chess.localPlayer) {
                            Text(
                                "CHECK",
                                fontSize = INFO_FONT_SIZE,
                                modifier = infoModifier
                            )
                        }
                    }
                    is showStalemate -> {
                        Text(
                            "STALEMATE",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }
                }
        }
        Column(
            Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH).background(MOVES_BACKGROUND_COLOR)
        ) {
                Text(
                    movesPlayed,
                    fontSize = MOVES_FONT_SIZE,
                )
        }
    }


}


@Composable
private fun drawVisualsWithoutAStartedGame(){
    Row(Modifier.background(Color(ORANGE)).height(WINDOW_HEIGHT)) {

        drawCoordinateNumbers()

        Column{

            drawCoordinateLetters()

            Box {

                buildBackgroundBoard()

            }
        }
        Column(
            Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH).background(Color.White)
        ) {
        //TODO: Add a text saying that the game has not started yet or maybe a button to start the game
        }
    }


}