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
import chess.UI.Compose.board.*
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.domain.commands.Result
import chess.domain.getPiecePossibleMovesFrom
import doesBelongTo
import doesNotBelongTo
import kotlinx.coroutines.delay



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



/**
 * This is the main entry point for our application, as our app starts here.
 */
@Composable
fun ApplicationScope.App(chessInfo: Chess) {
    val chess = remember { mutableStateOf(chessInfo) }
    val promotionType = remember { mutableStateOf("") }                     // The type of piece the user wants to promote to
    val isSelectingPromotion = remember { mutableStateOf(false) }           // Open the dialog to select a promotion
    val isAskingForName = remember { mutableStateOf(false) }                // Open the dialog to ask for a name
    val actionToDisplay = remember { mutableStateOf(ACTION.OPEN) }
    val clicked : MutableState<Clicked> = remember { mutableStateOf(NONE) }     // The state of click on a tile
    val showPossibleMoves = remember { mutableStateOf(true) }               // Show possible moves starts as true by default
    val move = remember { mutableStateOf("") }
    val possibleMovesList = remember { mutableStateOf(emptyList<Square>()) }      // List of possible moves for a piece
    val result : MutableState<Result> = remember { mutableStateOf(NONE()) }        // Result produced from making an action(moving, joining, etc)
    val showCheckInfo = remember { mutableStateOf(false) }
    val showCheckMateInfo = remember { mutableStateOf(false) }
    val movesToDisplay = remember { mutableStateOf("") }
    val areMovesUpdated = remember { mutableStateOf(false) }

    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCE_ICON_FILENAME),
        title = "Chess",
        resizable = false
    ) {

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
                onClose = { isAskingForName.value = false }
            ) {
                if (actionToDisplay.value == ACTION.JOIN) {
                    result.value = joinAction(it, chess.value)

                } else {
                    result.value = openAction(it, chess.value)
                }
                isAskingForName.value = false
            }
        }

        if (isSelectingPromotion.value) {

            selectPossiblePromotions(
                chess.value.currentPlayer,
                onClose = { isSelectingPromotion.value = false }
            ) {
                    promotionType.value = it
                    isSelectingPromotion.value = false
                }
        }

        LaunchedEffect(Unit) {
            while(true) {
                if(chess.value.currentGameId != null && chess.value.currentPlayer != chess.value.board.player) {
                    result.value = refreshBoardAction(chess.value)
                    areMovesUpdated.value = false
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
            val currentPlayer = chess.value.currentPlayer

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
                    //getPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList, chess.value.board,chess.value.currentPlayer, move.value)
                }
            }

        }

        if (clicked.value is FINISH) {
            dealWithMovement(
                clicked,
                possibleMovesList,
                showPossibleMoves.value,
                chess.value,
                move.value,
                result,
                promotionType,
                isSelectingPromotion,
            )
            areMovesUpdated.value = false
        }


        if (!areMovesUpdated.value) {
            val gameId = chess.value.currentGameId
            if(gameId != null ) {
                movesToDisplay.value = getMovesAsString(gameId, chess.value.dataBase)
                areMovesUpdated.value = true
            }
        }


        handleResult(result, chess, showCheckInfo, showCheckMateInfo, movesToDisplay, showPossibleMoves, possibleMovesList)

        val hasAGameStarted = chess.value.currentGameId != null

        if(hasAGameStarted){
            MaterialTheme {
                drawVisualsWithAStartedGame(chess.value,showCheckInfo.value,showCheckMateInfo.value,movesToDisplay.value,
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
        }else{
            MaterialTheme{
                drawVisualsWithoutAStartedGame()
            }
        }


    }
}



fun handleResult(
    result: MutableState<Result>,
    chess: MutableState<Chess>,
    showCheckInfo: MutableState<Boolean>,
    showCheckMateInfo: MutableState<Boolean>,
    movesPlayed: MutableState<String>,
    showPossibleMoves: MutableState<Boolean>,
    possibleMovesList: MutableState<List<Square>>
) {

    when (result.value) {
        is OK -> {
            val res = (result.value as OK)
            chess.value = res.chess
            if (res.moves != null) {
                movesPlayed.value = res.moves.toAString()
            }
            result.value = NONE()
            showCheckInfo.value = false
            showCheckMateInfo.value = false
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
        }
        is CHECK -> {
            //TODO: make check apper on the player received
            val res = (result.value as CHECK)
            chess.value = res.chess
            result.value = NONE()
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
            //TODO fix this, somewhere its wrong
            showCheckInfo.value = true
        }
        is CHECKMATE -> {
            showCheckMateInfo.value = true
        }
    }

}


@Composable
private fun dealWithMovement(
    clicked: MutableState<Clicked>,
    possibleMovesList: MutableState<List<Square>>,
    showPossibleMoves: Boolean,
    chess: Chess,
    move: String,
    result: MutableState<Result>,
    promotionType: MutableState<String>,
    isSelectingPromotion: MutableState<Boolean>
){
    val finish = clicked.value as FINISH
    val board = chess.board
    val finishSquare = finish.square.toSquare()
    val startSquare = move.toSquare()
    val currentPlayer = chess.currentPlayer
    val endPiece = chess.board.getPiece(finish.square.toSquare())
    val finalMoveString = remember { mutableStateOf("") }
    if(promotionType.value == "" && board.isTheMovementPromotable("$startSquare$finishSquare")) {
        isSelectingPromotion.value = true
    }

    finalMoveString.value =move + finish.square + "=" + promotionType.value


    val value = playAction(finalMoveString.value, chess)

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
    promotionType.value = ""
}





@Composable
private fun drawVisualsWithAStartedGame(
    chess: Chess,
    showCheckInfo: Boolean,
    showCheckMateInfo: Boolean,
    movesPlayed: String,
    checkIfIsAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    OnTileClicked : (square: Square) -> Unit
) {

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

                val info = if (chess.board.player == chess.currentPlayer) "Your turn" else "Waiting..."
                Text(
                    "Game:${gameId.id} | You:${chess.currentPlayer} | $info",
                    fontSize = INFO_FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )

                if(showCheckInfo){
                    Text(
                        "CHECK",
                        fontSize = INFO_FONT_SIZE,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp).background(Color.Red)
                    )
                }

                if(showCheckMateInfo){
                    Text(
                        "CHECKMATE",
                        fontSize = INFO_FONT_SIZE,
                        modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp).background(Color.Red)
                    )
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