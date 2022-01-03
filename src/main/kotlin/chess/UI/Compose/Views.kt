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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import chess.Chess
import chess.GameName
import chess.Storage.ChessDataBase
import chess.Storage.Move
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.exit
import chess.getPiecePossibleMovesFrom
import com.mongodb.client.MongoClient
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
fun ApplicationScope.App(chessInfo: Chess, driver: MongoClient) {
    val chess = remember { mutableStateOf(chessInfo) }
    val promotionType = remember { mutableStateOf("") }
    val isSelectingPromotion = remember { mutableStateOf(false) }           // open the dialog to select a promotion
    val isAskingForName = remember { mutableStateOf(false) }                // open the dialog to ask for a name
    val actionToDisplay = remember { mutableStateOf(ACTION.OPEN) }
    val clicked : MutableState<Clicked> = remember { mutableStateOf(NONE()) }
    val showPossibleMoves = remember { mutableStateOf(true) }       // show possible moves starts as true by default
    val move = remember { mutableStateOf("") }
    val selected = remember { mutableStateOf(emptyList<Square>()) }
    val isGameOver = remember { mutableStateOf(false) }
    val result : MutableState<Result> = remember { mutableStateOf(ERROR()) }


    Window(onCloseRequest = {exit(driver)},
        state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCE_ICON_FILENAME),
        title = "Chess"
    ) {
        if(!showPossibleMoves.value) selected.value = emptyList()

        if (isAskingForName.value) {
            getGameName(isAskingForName, actionToDisplay.value, chess.value,result)
        }
        if (isSelectingPromotion.value) {
            selectPossiblePromotions(promotionType, isSelectingPromotion)

        }

        //updateGameIfOtherPlayerMoved(chess)



        menu(actionToDisplay,isAskingForName,showPossibleMoves)

        if(clicked.value is START){
            val start = clicked.value as START

            val board = chess.value.board

            val startSquare = start.square.toSquare()
            val currentPlayer = chess.value.currentPlayer
            if(startSquare.doesNotBelongTo(currentPlayer, board)){
                clicked.value = NONE()
            }else{
                move.value = start.square
                getPossibleMovesIfOptionEnabled(showPossibleMoves.value,selected,chess.value,move.value)
            }

        }

        if(clicked.value is FINISH){
            dealWithMovement(clicked,selected,showPossibleMoves.value,chess,move,result,promotionType,isSelectingPromotion)
        }


        if(result.value is CONTINUE){
            val chessFromRes = (result.value as CONTINUE).chess
            chess.value = chessFromRes
        }


        MaterialTheme {
            drawVisuals(chess.value,clicked,selected.value,showPossibleMoves.value)
        }

    }
}

fun updateGameIfOtherPlayerMoved(chess: MutableState<Chess>) {
    val gameId = chess.value.currentGameId
    if(gameId != null) {
        val pNumber = if(chess.value.currentPlayer == Player.WHITE) 0 else 1

        val moveCount = chess.value.dataBase.getMoveCount(gameId)
        if(moveCount %2 != pNumber) {
            chess.value = refreshBoardAction(chess.value)
        }

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
    clicked: MutableState<Clicked>,
    selected: List<Square>,
    showPossibleMoves: Boolean,
){
    Column {
        for(i in 8 downTo 1) {
            Row {
                for (count in 0..7) {
                    val column = (count + 'a'.code).toChar()// convert to char
                    val square = "$column$i".toSquare()
                    val piece = board.getPiece(square)

                    val isSelected = clicked.value is START && (clicked.value as START).square == square.toString()
                    val isAPossibleMove = showPossibleMoves && selected.contains(square)

                    tile(piece ,isAPossibleMove,isSelected){
                        clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
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
    isAPossibleMove: Boolean,
    isTileSelected : Boolean ,
    onSelected: () -> Unit = { }
){
    val pieceImage = getResource(piece)
    val modifier = if(isTileSelected){
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
        if (isAPossibleMove) {
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
    selected: MutableState<List<Square>>,
    showPossibleMoves: Boolean,
    chess: MutableState<Chess>,
    move: MutableState<String>,
    result: MutableState<Result>,
    promotionType: MutableState<String>,
    isSelectingPromotion: MutableState<Boolean>,

    ) {
    val finish = clicked.value as FINISH
    val board = chess.value.board
    val finishSquare = finish.square.toSquare()
    val startSquare = move.value.toSquare()
    val currentPlayer = chess.value.currentPlayer
    val endPiece = chess.value.board.getPiece(finish.square.toSquare())
    val finalMoveString = remember { mutableStateOf("") }
    if(promotionType.value != ""){
        finalMoveString.value =move.value + finish.square + "=" + promotionType.value
    }else {
        finalMoveString.value = move.value + finish.square
    }



        val value = playAction(finalMoveString.value, chess.value)

        result.value = value
        when {
            value is ERROR && board.isTheMovementPromotable(finalMoveString.value) -> {
                isSelectingPromotion.value = true

            }
            value is ERROR && startSquare == finishSquare -> {
                clicked.value = NONE()
                clearPossibleMovesIfOptionEnabled(showPossibleMoves, selected)
            }
            value is ERROR && finishSquare.doesBelongTo(currentPlayer, chess.value.board) -> {
                clicked.value = START(finish.square)
                clearPossibleMovesIfOptionEnabled(showPossibleMoves, selected)
            }
            value is ERROR && endPiece != null && endPiece.player != currentPlayer -> {
                clicked.value = NONE()
                move.value = ""
                clearPossibleMovesIfOptionEnabled(showPossibleMoves, selected)
            }
            value is ERROR && !selected.value.contains(finishSquare) -> clicked.value = START(startSquare.toString())
            else -> {
                clicked.value = NONE()
                move.value = ""
                promotionType.value = ""
                clearPossibleMovesIfOptionEnabled(showPossibleMoves, selected)
            }

        }

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
    clicked: MutableState<Clicked>,
    selected: List<Square>,
    showPossibleMoves: Boolean
) {

    val gameId = chess.currentGameId
    Row(Modifier.background(Color(ORANGE))) {
        drawCoordinateNumbers()
        Column {
            drawCoordinateLetters()
            Box {
                buildBackgroundBoard()
                if(gameId != null) {
                    boardToComposable(chess.board,clicked,selected,showPossibleMoves)
                }
            }
            if (gameId != null) {
                val info = if(chess.board.player == chess.currentPlayer) "Your turn" else "Waiting..."
                Text(
                    "Game:${gameId.id} | You:${chess.currentPlayer} | $info",
                    fontSize = INFO_FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )
            }


        }
        Column(Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH) .background(Color.White)) {
            if(gameId != null){
                /*
                TODO("always getting the moves is causing lag")
               val moves =  getMovesAsString(gameId,chess.dataBase)
                Text(
                    moves,
                    fontSize = MOVES_FONT_SIZE,
                    )

                 */
            }
        }

    }

}

//fun ifOtherPlayerMoved(block:Block )
//fun if_else(condition:Boolean,block:Block,block2:Block)



/**
 * Makes the menu bar at the top of the screen
 * @param action    the action to display
 * @param askingName  a [Boolean] that tells if the user is asking for a name
 * @param showPossibleMoves     a [Boolean] that tells if the user wants to see possible moves
 */
@Composable
private fun FrameWindowScope.menu(action : MutableState<ACTION>, askingName : MutableState<Boolean>,showPossibleMoves : MutableState<Boolean>){
    MenuBar {
        Menu("Game", mnemonic = 'G') {
            Item("Open", onClick = { action.value = ACTION.OPEN; askingName.value = true })
            Item("Join", onClick = { action.value = ACTION.JOIN; askingName.value = true })
        }
        Menu("Options", mnemonic = 'O') {
            CheckboxItem("Show Possible Moves",checked = showPossibleMoves.value, onCheckedChange = { showPossibleMoves.value = it })
        }
    }
}

/**
 * Creates a Dialog that gives the user 4 options to choose the promotion piece
 */
@Composable
@Preview
private fun selectPossiblePromotions(Promotion: MutableState<String>, isSelectingPromotion : MutableState<Boolean>){
    Dialog(
        onCloseRequest = { isSelectingPromotion.value = false },
        title = "Select the piece you want to promote to",
    ) {
        Column {
            Row(Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { Promotion.value = "Q" ; isSelectingPromotion.value = false },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("Q", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "R" ; isSelectingPromotion.value = false },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("R", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "B"  ; isSelectingPromotion.value = false },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("B", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "N" ; isSelectingPromotion.value = false },
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
    isAskingForName: MutableState<Boolean>,
    action: ACTION,
    chess: Chess,
    result: MutableState<Result>
){
    val gameName = remember { mutableStateOf("") }

    Dialog(
        onCloseRequest = { isAskingForName.value = false },
        title = "Insert Game Name",
    ) {
        Column{
            Text("Please insert the name of the game to ${action.text}")
            TextField(
                value = gameName.value,
                onValueChange = { gameName.value = it }
            )
            Button(
                onClick = {
                    isAskingForName.value = false
                    if (action == ACTION.OPEN) result.value = openAction(gameName.value,chess)
                    else result.value = joinAction(gameName.value,chess)
                }
            ) {
                Text("Confirm")
            }
        }
    }

}


