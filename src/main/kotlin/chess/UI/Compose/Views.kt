package chess.UI.Compose
import Bishop
import King
import Knight
import Pawn
import Piece
import Queen
import Rook
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
import chess.domain.Player
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare
import chess.domain.commands.*
import chess.exit
import chess.getPiecePossibleMovesFrom
import com.mongodb.client.MongoClient
import doesNotBelongTo


private const val BACKGROUND_COLOR_1 = 0xFF789454
private const val BACKGROUND_COLOR_2 = 0xFFfcf1e8
private const val ORANGE = 0xFFB5651E
private val TILE_SIZE = 60.dp
private val MOVES_TEXT_SIZE_HEIGHT = 500.dp
private val MOVES_TEXT_SIZE_WIDTH = 200.dp
private val COORDINATES_FONT_SIZE = 21.sp
private val FONT_SIZE = 18.sp
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


private enum class ACTION{
    OPEN,
    JOIN
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
private fun boardToComposable(chess: MutableState<Chess>, clicked: MutableState<Clicked>, selected: MutableState<List<Square>>, showPossibleMoves: MutableState<Boolean>){
    Column {
        for(i in 8 downTo 1) {
            Row {
                for (count in 0..7) {
                    val column = (count + 'a'.code).toChar()// convert to char
                    val square = "$column$i".toSquare()
                    val piece = chess.value.board.getPiece(square)

                    tile(piece ,clicked, square, selected,showPossibleMoves)
                }
            }
        }
    }
}

@Composable
private fun tile(
    piece: Piece?,
    clicked: MutableState<Clicked>,
    square: Square,
    selected: MutableState<List<Square>>,
    showPossibleMoves : MutableState<Boolean>
){
    val pieceImage = remember { mutableStateOf("empty-tile")}
    if (piece != null) {
        val pieceColor = piece.player

        pieceImage.value = when (piece) {
            is Pawn -> RESOURCE_PAWN_FILENAME
            is Rook -> RESOURCE_ROOK_FILENAME
            is Knight -> RESOURCE_KNIGHT_FILENAME
            is Bishop -> RESOURCE_BISHOP_FILENAME
            is Queen -> RESOURCE_QUEEN_FILENAME
            is King -> RESOURCE_KING_FILENAME
            else -> {"empty-tile"}
        }
        pieceImage.value = if (pieceColor == Player.WHITE) "w_${pieceImage.value}"
        else "b_${pieceImage.value}"
    }

    val modifier = if(clicked.value is START && (clicked.value as START).square == square.toString()){
        Modifier.clickable(
            onClick = {
                clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
            }).border(4.dp,Color.Red)
        }
        else {
        Modifier.clickable(
            onClick = {
                clicked.value = if (clicked.value is NONE) START(square.toString()) else FINISH(square.toString())
            })
    }


    Box(modifier = modifier) {
        if (pieceImage.value != "empty-tile") {
            Image(
                painter = painterResource(pieceImage.value),
                modifier = Modifier.size(TILE_SIZE).align(Alignment.Center),
                contentDescription = null
            )

        } else {
            Spacer(modifier = Modifier.size(TILE_SIZE))
        }
        if(selected.value.isNotEmpty() && showPossibleMoves.value){
            if(selected.value.contains(square)){
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

}

@Composable
private fun dealWithMovement(
    clicked: MutableState<Clicked>,
    selected: MutableState<List<Square>>,
    showPossibleMoves: MutableState<Boolean>,
    chess: MutableState<Chess>,
    move: MutableState<String>,
    result: MutableState<Result>
){
    val promotionType = remember { mutableStateOf("") }
    val finish = clicked.value as FINISH

    val board = chess.value.board
    val finishSquare = finish.square.toSquare()
    val startSquare = move.value.toSquare()
    val currentPlayer = chess.value.currentPlayer
    val startPiece = chess.value.board.getPiece(startSquare)
    val endPiece = chess.value.board.getPiece(finish.square.toSquare())
    move.value = move.value + finish.square

/*
            if(startSquare == finishSquare) {
                clicked.value = NONE()
                move.value = ""
                if(showPossibleMoves.value){
                    selected.value = emptyList()
                }


            }else {

                when{
                   /*
                    piece == null -> {

                        clicked.value = START(move.value)

                    }
                    */

                    finishSquare.doesBelongTo(currentPlayer,board) ->  {

                        clicked.value = START(finish.square)
                    }
                    /*
                    else -> {
                        if(showPossibleMoves.value){
                            selected.value = emptyList()
                        }

                        move.value = move.value + finish.square
                        clicked.value = NONE()
                    }
                     */
                }

 */

    if(board.isTheMovementPromotable(move.value)){
        selectPossiblePromotions(promotionType)
        move.value +=  promotionType.value


    }else {
        result.value = playAction(move.value, chess.value)

        clicked.value = NONE()
        move.value = ""
        if(showPossibleMoves.value){
            selected.value = emptyList()
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

/**
 * Builds the UI for the chessboard(background and the pieces).
 * @param board The board to be displayed.
 * @param clicked The state of the clicked square.
 */
@Composable
private fun chessBoard(chess: MutableState<Chess>, clicked: MutableState<Clicked>, selected: MutableState<List<Square>>, showPossibleMoves : MutableState<Boolean>) {
    Box {
        buildBackgroundBoard()
        boardToComposable(chess,clicked,selected,showPossibleMoves)
    }
}

/**
 * This is the main entry point for our application, as our app starts here.
 */
@Composable
fun ApplicationScope.App(chessInfo: Chess, driver: MongoClient) {
        val chess = remember { mutableStateOf(chessInfo) }
        val isAskingForName = remember { mutableStateOf(false) }
        val actionToDisplay = remember { mutableStateOf(ACTION.OPEN) }
        val clicked : MutableState<Clicked> = remember { mutableStateOf(NONE()) }
        val showPossibleMoves = remember { mutableStateOf(true) } // show possible moves starts as true by default
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
            getGameName(isAskingForName, actionToDisplay, chess,result)
        }


        println("recomposition")

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
                if(showPossibleMoves.value) {
                    val moves = chess.value.getPiecePossibleMovesFrom(startSquare)
                    if (moves.isNotEmpty()) {
                        val possibleMoves = moves.map { it.endSquare }
                        selected.value = possibleMoves
                    }
                }

            }

        }

        if(clicked.value is FINISH){
            dealWithMovement(clicked,selected,showPossibleMoves,chess,move,result)
        }

        if(result.value is CONTINUE){
            val chessFromRes = (result.value as CONTINUE).chess
            chess.value = chessFromRes
        }


        MaterialTheme {
            drawVisuals(chess,clicked,selected,showPossibleMoves,move)
        }

    }
}

@Composable
private fun drawVisuals(
    chess: MutableState<Chess>,
    clicked: MutableState<Clicked>,
    selected: MutableState<List<Square>>,
    showPossibleMoves: MutableState<Boolean>,
    move: MutableState<String>
) {

    val gameId = chess.value.currentGameId
    Row(Modifier.background(Color(ORANGE))) {
        Column(Modifier.padding(top = MENU_HEIGHT)) {
            for (i in 8 downTo 1) {
                Text(
                    "$i",
                    fontSize = COORDINATES_FONT_SIZE,
                    modifier = Modifier.padding(top = PADDING_BETWEEN_NUMBERS, bottom = PADDING_BETWEEN_NUMBERS , start = TEXT_BORDER_PADDING, end = TEXT_BORDER_PADDING)
                )
            }
        }
        Column {
            Row {
                for (i in 0..7) {
                    Text(
                        "${(i + 'a'.code).toChar()}",
                        fontSize = COORDINATES_FONT_SIZE,
                        modifier = Modifier.padding(start = PADDING_BETWEEN_LETTERS, end = PADDING_BETWEEN_LETTERS, top = TEXT_BORDER_PADDING, bottom = TEXT_BORDER_PADDING)
                    )
                }
            }
            Box {
                buildBackgroundBoard()
                if(gameId != null) {
                    boardToComposable(chess,clicked,selected,showPossibleMoves)
                }
            }
            if (gameId != null) {
                Text(
                    "Game:${gameId.id} | You:${chess.value.currentPlayer} | ",
                    fontSize = FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )
            }


        }
        Column(Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH) .background(Color.White)) {
            if(gameId != null){
                Text(
                    "move:${move.value} \n" +
                            "clicked:${clicked.value} \n" ,
                    fontSize = 10.sp,

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


@Composable
private fun selectPossiblePromotions(Promotion: MutableState<String>){
    Column {
        Row(Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { Promotion.value = "Q" },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("Q", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "R" },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("R", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "B" },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("B", fontSize = 24.sp)
                }
                Button(
                    onClick = { Promotion.value = "N" },
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    Text("N", fontSize = 24.sp)
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
private fun getGameName(isAskingForName: MutableState<Boolean>,action: MutableState<ACTION>, chess : MutableState<Chess>, result : MutableState<Result>){
    val gameName = remember { mutableStateOf("") }

    Dialog(
        onCloseRequest = { isAskingForName.value = false },
        title = "Insert Game Name",
    ) {
        Column{
            Text("Please insert the name of the game to ${action.value}")
            TextField(
                value = gameName.value,
                onValueChange = { gameName.value = it }
            )
            Button(
                onClick = {
                    isAskingForName.value = false
                    if (action.value == ACTION.OPEN) result.value = openAction(gameName.value,chess.value)
                    else result.value = joinAction(gameName.value,chess.value)
                }
            ) {
                Text("Confirm")
            }
        }
    }

}



