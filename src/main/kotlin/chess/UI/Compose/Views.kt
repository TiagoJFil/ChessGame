package chess.UI.Compose
import Bishop
import Board
import King
import Knight
import Pawn
import Piece
import Queen
import Rook
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.Chess
import chess.GameName
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.toGameNameOrNull
import doesBelongTo
import doesNotBelongTo


private const val BACKGROUND_COLOR_1 = 0xFF789454
private const val BACKGROUND_COLOR_2 = 0xFFfcf1e8
private const val ORANGE = 0xFFB5651E
private val TILE_SIZE = 60.dp
private val MOVES_TEXT_SIZE_HEIGHT = 500.dp
private val MOVES_TEXT_SIZE_WIDTH = 200.dp
private val COORDINATES_FONT_SIZE = 21.sp
private val MOVES_FONT_SIZE = 16.sp
private val INFO_FONT_SIZE = 18.sp
private val MENU_HEIGHT = 27.dp
private val TEXT_BORDER_PADDING = 4.dp
private val PADDING_BETWEEN_NUMBERS = 18.dp
private val PADDING_BETWEEN_LETTERS = 24.dp
private val WINDOW_HEIGHT = 600.dp

private const val RESOURCE_PAWN_FILENAME = "pawn.png"
private const val RESOURCE_ROOK_FILENAME = "rook.png"
private const val RESOURCE_KNIGHT_FILENAME = "knight.png"
private const val RESOURCE_BISHOP_FILENAME = "bishop.png"
private const val RESOURCE_QUEEN_FILENAME = "queen.png"
private const val RESOURCE_KING_FILENAME = "king.png"
private const val RESOURCE_ICON_FILENAME = "favicon.ico"
private const val RESOURCE_SELECTED_TILE_FILENAME = "tile.png"


private enum class ACTION(val text: String) {
    OPEN("Open"),
    JOIN("Join")
}
/**
 * Represents the possible Colors a tile can take
 */
private enum class Colors {
    WHITE,
    BLACK;

    operator fun not(): Colors {
        return when (this) {
            WHITE -> BLACK
            BLACK -> WHITE
        }
    }
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
    val clicked : MutableState<Clicked> = remember { mutableStateOf(NONE()) }    // The state of click on a tile
    val showPossibleMoves = remember { mutableStateOf(true) }               // Show possible moves starts as true by default
    val move = remember { mutableStateOf("") }
    val possibleMovesList = remember { mutableStateOf(emptyList<Square>()) }      // List of possible moves for a piece
    val result : MutableState<Result> = remember { mutableStateOf(ERROR()) }      // Result produced from making an action(moving, joining, etc)
    val showCheckInfo = remember { mutableStateOf(false) }
    val showCheckMateInfo = remember { mutableStateOf(false) }
    val movesPlayed = remember { mutableStateOf("") }
    val areMovesUpdated = remember { mutableStateOf(false) }


    Window(onCloseRequest = ::exitApplication,
        state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCE_ICON_FILENAME),
        title = "Chess",
        resizable = false
    ) {

        menu(onClickOpen = {
            actionToDisplay.value = ACTION.OPEN; isAskingForName.value = true
        },
            onClickJoin = {
                actionToDisplay.value = ACTION.JOIN; isAskingForName.value = true
            },
            onClickShowMoves = {
                showPossibleMoves.value = it
            })


        if (!showPossibleMoves.value) possibleMovesList.value = emptyList()

        if (isAskingForName.value) {
            getGameName(
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
            selectPossiblePromotions(isSelectingPromotion, onClose = { isSelectingPromotion.value = false }) {
                promotionType.value = it
                isSelectingPromotion.value = false
            }
        }

        updateGameIfOtherPlayerMoved(chess)



        if (clicked.value is START) {
            val start = clicked.value as START

            val board = chess.value.board

            val startSquare = start.square.toSquare()
            val currentPlayer = chess.value.currentPlayer

            if (currentPlayer != board.player) {
                clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
                clicked.value = NONE()
            } else {

                if (startSquare.doesNotBelongTo(currentPlayer, board)) {
                    clicked.value = NONE()
                } else {
                    move.value = start.square
                    getPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList, chess.value, move.value)
                }
            }

        }

        if (clicked.value is FINISH) {
            dealWithMovement(
                clicked,
                possibleMovesList,
                showPossibleMoves.value,
                chess.value,
                move,
                result,
                promotionType,
                isSelectingPromotion
            )

            areMovesUpdated.value = false
        }

        if (!areMovesUpdated.value) {
            ifOtherPlayerMoved(chess.value) {
                val gameId = chess.value.currentGameId
                require(gameId != null)
                movesPlayed.value = getMovesAsString(gameId, chess.value.dataBase)
                areMovesUpdated.value = true
            }
        }

        handleResult(result, chess, showCheckInfo, showCheckMateInfo, movesPlayed, showPossibleMoves, possibleMovesList)

        MaterialTheme {
            drawVisuals(chess.value,showCheckInfo.value,showCheckMateInfo.value,movesPlayed.value,
                checkIfisAPossibleMove = { square ->
                    showPossibleMoves.value && possibleMovesList.value.contains(square)
                                         },
                checkIfTileIsSelected =   { square ->
                    clicked.value is START && (clicked.value as START).square == square.toString()
                }
            ) { square ->
                clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
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
        is CONTINUE -> {
            val res = (result.value as CONTINUE)
            chess.value = res.chess
            if (res.moves != null) {
                movesPlayed.value = res.moves.toAString()
            }
            result.value = ERROR()
            showCheckInfo.value = false
            showCheckMateInfo.value = false
            clearPossibleMovesIfOptionEnabled(showPossibleMoves.value, possibleMovesList)
        }
        is CHECK -> {
            //TODO fix this, somewhere its wrong
            showCheckInfo.value = true
        }
        is CHECKMATE -> {
            showCheckMateInfo.value = true
        }
    }

}

@Composable
fun updateGameIfOtherPlayerMoved(chess: MutableState<Chess>) {
    ifOtherPlayerMoved(chess.value){
        chess.value = refreshBoardAction(chess.value)
    }
}

@Composable
fun ifOtherPlayerMoved(chess: Chess,block: @Composable () -> Unit){
    val gameId = chess.currentGameId
    if(gameId != null && chess.currentPlayer != chess.board.player) {

        val pNumber = if(chess.currentPlayer == Player.WHITE) 0 else 1
        val moveCount = chess.dataBase.getMoveCount(gameId)

        if(moveCount %2 != pNumber)
                block()
    }
}

/**
 * Builds the UI for the background chessboard.
 */
@Composable
private fun buildBackgroundBoard(){
    Row{
        for(i in 1..8) {
            if (i % 2 == 0)
                Column {
                    for (count in 1..8) {
                        val tileColor =
                            if (count % 2 == 0) Colors.WHITE
                            else Colors.BLACK
                        BackgroundTile(tileColor)
                    }
                }
            else{
                Column {
                    for (count in 1..8) {
                        val tileColor =
                            if (count % 2 == 1) Colors.WHITE
                            else Colors.BLACK
                        BackgroundTile(tileColor)
                    }
                }
            }
        }
    }
}

/**
 * Places the pieces from the [board] received on the chessBoard.
 */
@Composable
private fun boardToComposable(
    board: Board,
    checkIfisAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    OnTileClicked : (square: Square) -> Unit
){
    Column {
        for(i in 8 downTo 1) {
            Row {
                for (count in 0..7) {
                    val column = (count + 'a'.code).toChar()// convert to char
                    val square = "$column$i".toSquare()
                    val piece = board.getPiece(square)

                    tile(piece, square, checkIfisAPossibleMove, checkIfTileIsSelected){
                        OnTileClicked(square)
                    }
                }
            }
        }
    }
}

/**
 * Gets a resource name associated with the piece received, if it exists. else returns null.
 * @param piece The piece to get the resource for.
 */
private fun getResource(piece: Piece?):String?{
    if(piece == null) return null
    val pieceColor = piece.player

    val resource = when(piece){
        is Pawn -> RESOURCE_PAWN_FILENAME
        is Rook -> RESOURCE_ROOK_FILENAME
        is Knight -> RESOURCE_KNIGHT_FILENAME
        is Bishop -> RESOURCE_BISHOP_FILENAME
        is Queen -> RESOURCE_QUEEN_FILENAME
        is King -> RESOURCE_KING_FILENAME
        else -> null
    }

    val color = if (pieceColor == Player.WHITE) "w_"
    else "b_"

    return "$color$resource"
}

/**
 * Draws a tile on the chessboard.
 * The Tile could be a piece, or a empty square and if [showPossibleMoves] is active, also a circle.
 * @param piece             The piece to draw on the tile, may be null(empty square).
 * @param isAPossibleMove   If the tile is a possible move for other piece.
 * @param isSelected        If the tile is selected by the user.
 * @param onClick           The function to call when the tile is clicked.
 */
@Composable
private fun tile(
    piece: Piece?,
    square : Square ,
    checkIfisAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    onSelected: () -> Unit = { }
){
    val pieceImage = getResource(piece)
    val modifier = if(checkIfTileIsSelected(square)){
        Modifier.clickable( onClick = { onSelected() }).border(4.dp,Color.Red)
    }
    else {
        Modifier.clickable( onClick = { onSelected() })
    }

    Box(modifier = modifier) {
        if (pieceImage != null) {
            Image(
                painter = painterResource(pieceImage),
                modifier = Modifier.size(TILE_SIZE).align(Alignment.Center),
                contentDescription = null
            )

        } else {
            Spacer(modifier = Modifier.size(TILE_SIZE))
        }
        if (checkIfisAPossibleMove(square)) {
            Box(modifier = Modifier.size(TILE_SIZE)) {
                Image(
                    painter = painterResource(RESOURCE_SELECTED_TILE_FILENAME),
                    modifier = Modifier.size(TILE_SIZE).align(Alignment.Center),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun dealWithMovement(
    clicked: MutableState<Clicked>,
    possibleMovesList: MutableState<List<Square>>,
    showPossibleMoves: Boolean,
    chess: Chess,
    move: MutableState<String>,
    result: MutableState<Result>,
    promotionType: MutableState<String>,
    isSelectingPromotion: MutableState<Boolean>,
){
    val finish = clicked.value as FINISH
    val board = chess.board
    val finishSquare = finish.square.toSquare()
    val startSquare = move.value.toSquare()
    val currentPlayer = chess.currentPlayer
    val endPiece = chess.board.getPiece(finish.square.toSquare())
    val finalMoveString = remember { mutableStateOf("") }
    if(promotionType.value != ""){
        finalMoveString.value =move.value + finish.square + "=" + promotionType.value
    }
    else {
        finalMoveString.value = move.value + finish.square
    }
        val value = playAction(finalMoveString.value, chess)

        result.value = value
        when {
            value is ERROR && board.isTheMovementPromotable(finalMoveString.value) -> {
                isSelectingPromotion.value = true
            }
            value is ERROR && startSquare == finishSquare -> {
                clicked.value = NONE()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
            }
            value is ERROR && finishSquare.doesBelongTo(currentPlayer, chess.board) -> {
                clicked.value = START(finish.square)

                clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
            }
            value is ERROR && endPiece != null && endPiece.player != currentPlayer -> {
                clicked.value = NONE()

                clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
            }
            value is ERROR && !possibleMovesList.value.contains(finishSquare) -> clicked.value = START(startSquare.toString())
            else -> {
                clicked.value = NONE()

                clearPossibleMovesIfOptionEnabled(showPossibleMoves, possibleMovesList)
            }
        }
    promotionType.value = ""
}


/**
 * Builds a Background Tile of the chessboard with the [Colors] received.
 * @param tileColor The color of the tile.
 */
@Composable
private fun BackgroundTile(tileColor: Colors) {
    val color =
        if (tileColor == Colors.BLACK) Color(BACKGROUND_COLOR_1)
        else Color(BACKGROUND_COLOR_2)

    Box(Modifier.background(color).size(TILE_SIZE))
}

@Composable
private fun drawCoordinateNumbers() {
    Column(Modifier.padding(top = MENU_HEIGHT)) {
        for (i in 8 downTo 1) {
            Text(
                "$i",
                fontSize = COORDINATES_FONT_SIZE,
                modifier = Modifier.padding(
                    top = PADDING_BETWEEN_NUMBERS,
                    bottom = PADDING_BETWEEN_NUMBERS,
                    start = TEXT_BORDER_PADDING,
                    end = TEXT_BORDER_PADDING
                )
            )

        }
    }
}

@Composable
private fun drawCoordinateLetters(){
    Row {
        for (i in 0..7) {
            Text(
                "${(i + 'a'.code).toChar()}",
                fontSize = COORDINATES_FONT_SIZE,
                modifier = Modifier.padding(start = PADDING_BETWEEN_LETTERS, end = PADDING_BETWEEN_LETTERS, top = TEXT_BORDER_PADDING, bottom = TEXT_BORDER_PADDING)
            )
        }
    }
}

@Composable
private fun drawVisuals(
    chess: Chess,
    showCheckInfo: Boolean,
    showCheckMateInfo: Boolean,
    movesPlayed: String,
    checkIfisAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    OnTileClicked : (square: Square) -> Unit
) {

    val gameId = chess.currentGameId
    Row(Modifier.background(Color(ORANGE)).height(WINDOW_HEIGHT)) {

        drawCoordinateNumbers()

        Column{

            drawCoordinateLetters()

            Box {
                buildBackgroundBoard()
                if (gameId != null) {
                    boardToComposable( chess.board, checkIfisAPossibleMove, checkIfTileIsSelected, OnTileClicked)
                }
            }

            if (gameId != null) {
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

        }
        Column(
            Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH).background(Color.White)
        ) {
            if(gameId != null){
                Text(
                    movesPlayed,
                    fontSize = MOVES_FONT_SIZE,
                )
            }

        }
    }


}

/**
 * Makes the menu bar at the top of the screen
 * @param action    the action to display
 * @param askingName  a [Boolean] that tells if the user is asking for a name
 * @param showPossibleMoves     a [Boolean] that tells if the user wants to see possible moves
 */
@Composable
private fun FrameWindowScope.menu(onClickOpen : () -> Unit,
                                  onClickJoin : () -> Unit,
                                  onClickShowMoves : (value : Boolean) -> Unit,
){
    val showMovesOption = remember { mutableStateOf(true) }
    MenuBar {
        Menu("Game", mnemonic = 'G') {
            Item("Open", onClick = { onClickOpen() })
            Item("Join", onClick = { onClickJoin() })
        }
        Menu("Options", mnemonic = 'O') {
            CheckboxItem("Show Possible Moves",checked = showMovesOption.value, onCheckedChange = { onClickShowMoves(showMovesOption.value) ; showMovesOption.value = it })
        }
    }
}

/**
 * Creates a Dialog that gives the user 4 options to choose the promotion piece
 */
@Composable
@Preview
private fun selectPossiblePromotions(isSelectingPromotion : MutableState<Boolean>,
                                     onClose : () -> Unit,
                                     updateValue : (promotionPiece: String) -> Unit){

    Dialog(
        onCloseRequest = { onClose() },
        title = "Select the piece you want to promote to",
    ) {
        Column {
            Row(Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { updateValue("Q") },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("Q", fontSize = 24.sp)
                }
                Button(
                    onClick = { updateValue("R") },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("R", fontSize = 24.sp)
                }
                Button(
                    onClick = { updateValue("B") },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("B", fontSize = 24.sp)
                }
                Button(
                    onClick = { updateValue("N") },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("N", fontSize = 24.sp)
                }
            }
        }
    }
}

/**
 * This function asks the user for a name and saves it in the [chess] object
 * @param isAskingForName  a [Boolean] that tells if the user is asking for a name
 * @param action    the action to display
 * @param chess     a [chess] object that contains the current state of the game
 */
@Composable
private fun getGameName(
    onClose : () -> Unit,
    Action : (name : GameName) -> Unit
){
    val input = remember { mutableStateOf("") }

    val filterGameName = {
        val gameId = input.value.toGameNameOrNull()
        if(gameId != null){
            Action(gameId)
        }else{
            input.value = ""
        }

    }
    Dialog(
        onCloseRequest = {onClose() },
        title = "Insert Game Name",
        resizable = false,
        state = DialogState(size = DpSize(width = 400.dp, height = 200.dp))

    ) {
        Column{
            Text(
                "Please insert the name of the game to enter",
                fontSize = 19.sp,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            TextField(
                value = input.value,
                onValueChange = { input.value = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp)
            )
            Button(
                onClick = { filterGameName() },
                modifier = Modifier.absoluteOffset(150.dp,5.dp).size(100.dp,50.dp)
            ) {
                Text("Confirm")
            }
        }
    }

}


