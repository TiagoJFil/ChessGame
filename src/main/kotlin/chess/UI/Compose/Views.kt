package chess.UI.Compose
import Bishop
import Board
import King
import Knight
import Pawn
import Piece
import PieceMove
import Queen
import Rook
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import chess.domain.Player
import chess.domain.board_components.toSquare
import isel.leic.tds.storage.DbMode
import isel.leic.tds.storage.getDBConnectionInfo
import isel.leic.tds.storage.mongodb.createMongoClient
import org.junit.Test

private const val BACKGROUND_COLOR_1 = 0xFF789454
private const val BACKGROUND_COLOR_2 = 0xFFfcf1e8
private const val ORANGE = 0xFFB5651E
private val TILE_SIZE = 75.dp
private enum class RESOURCES(val FILENAME: String){
    PAWN("pawn.png"),
    ROOK("rook.png"),
    KNIGHT("knight.png"),
    BISHOP("bishop.png"),
    QUEEN("queen.png"),
    KING("king.png"),
    ICON("favicon.ico")
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
fun buildBackgroundBoard(){
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

/*
@Composable
fun Modifier.selectPiece(){
    this.background(painter = painterResource(id = R.drawable.highlight))

}
*/
/**
 * Places the pieces from the [board] received on the chessBoard.
 */
@Composable
fun boardToComposable(board: Board){
    Column {
        for(i in 8 downTo 1) {
                Row {
                    for (count in 0..7) {
                        val column = (count + 'a'.code).toChar()// convert to char
                        val square = "$column$i".toSquare()
                        val piece = board.getPiece(square)
                        tile(piece)
                    }
                }
        }
    }
}

@Composable
private fun tile(piece: Piece?){
    var pieceImage = "empty-tile"
    if (piece != null) {
        val pieceColor = piece.player
        pieceImage = when (piece) {
            is Pawn -> RESOURCES.PAWN.FILENAME
            is Rook -> RESOURCES.ROOK.FILENAME
            is Knight -> RESOURCES.KNIGHT.FILENAME
            is Bishop -> RESOURCES.BISHOP.FILENAME
            is Queen -> RESOURCES.QUEEN.FILENAME
            is King -> RESOURCES.KING.FILENAME
            else -> {"empty-tile"}
        }
        pieceImage = if (pieceColor == Player.WHITE) "w_$pieceImage"
                         else "b_$pieceImage"
    }
    val boxmodifier = Modifier.clickable {  }
    Box( //modifier = boxmodifier.border(4.dp,Color.Red),                ----------------------------------------------------------------------------
        ) {
        if (pieceImage != "empty-tile") {
            Image(
                painter = painterResource(pieceImage),
                modifier = Modifier.size(TILE_SIZE).align(Alignment.Center),
                // modifier = Modifier.size(60.dp).padding(5.dp),
                contentDescription = null
            )
        } else {
            Spacer(Modifier.size(TILE_SIZE))
        }
    }

}


@Composable
private fun BackgroundTile(tileColor: Colors) {
    val color =
        if (tileColor == Colors.BLACK) Color(BACKGROUND_COLOR_1)
        else Color(BACKGROUND_COLOR_2)

    Box(Modifier.background(color).size(TILE_SIZE)) {}
}


@Composable
fun chessBoard(board: Board) {
    Box {
        buildBackgroundBoard()
        boardToComposable(board)
    }


}

@Preview
@Composable
fun App(chess: MutableState<Chess>) {

    MaterialTheme {
        //val chess = remember { mutableStateOf(chessInfo)}
        val gameId = chess.value.currentGameId
        Row(Modifier.background(Color(ORANGE))) {
            Column(Modifier.padding(top = 32.dp)) {
                for (i in 8 downTo 1) {
                    Text(
                        "$i",
                        fontSize = 21.sp,
                        modifier = Modifier.padding(top = 26.dp, bottom = 25.dp, start = 6.dp, end = 6.dp)
                    )
                }
            }
            Column {
                Row {
                    for (i in 0..7) {
                        Text(
                            "${(i + 'a'.code).toChar()}",
                            fontSize = 21.sp,
                            modifier = Modifier.padding(start = 32.dp, end = 31.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }
                Box {
                    chessBoard(chess.value.board)
                }
                if(gameId != null){
                    Text(
                        "Game:${gameId.id} | You:${chess.value.currentPlayer} | ",
                        fontSize = 21.sp,
                        modifier = Modifier.padding(start = 4.dp, end = 31.dp, top = 32.dp, bottom = 4.dp)
                    )
                }


            }
            Column(Modifier.padding(32.dp).height(640.dp).background(Color.White)) {
                Text(
                    "TODO ON THE RIGHT",
                    fontSize = 21.sp,

                )
            }

        }


    }
}

@Composable
fun showPossibleMoves(moves:List<PieceMove>){

}
// chess: MutableState<Chess>
@Composable
fun FrameWindowScope.menu(action : MutableState<String>, askingName : MutableState<Boolean>, chess: MutableState<Chess>) {
    MenuBar {
        Menu("Game", mnemonic = 'G') {
            Item("Open", onClick = { action.value = "open"; askingName.value = true ; chess.value.currentPlayer = Player.WHITE})
            Item("Join", onClick = { action.value = "join"; askingName.value = true ; chess.value.currentPlayer = Player.BLACK})
        }
        Menu("Options", mnemonic = 'O') {
            Item("Show Possible Moves", onClick = { action.value = "Clicked the other option" })
        }
    }
}
@Composable
fun getGameName(isAskingForNameOpen: MutableState<Boolean>,action: MutableState<String>, chess : MutableState<Chess>){

    if (isAskingForNameOpen.value) {
        val gameName = remember { mutableStateOf("") }
        Dialog(
            onCloseRequest = { isAskingForNameOpen.value = false },
            title = "Close the document without saving?",
        ) {
            Column{
                Text("Please insert the name of the game to ${action.value}")
                TextField(
                    value = gameName.value,
                    onValueChange = { gameName.value = it; chess.value.currentGameId = GameName(it) }
                )
                Button(
                    onClick = { isAskingForNameOpen.value = false },
                ) {
                    Text("Confirm")
                }
            }
        }
    }

}

@Test
fun main() = application {
    val board = Board()
    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()
    val chessGame = Chess(Board(), ChessDataBase(driver.getDatabase(dbInfo.dbName)), null, Player.WHITE )

    val isAskingForName = remember { mutableStateOf(false) }
    val action = remember { mutableStateOf("") }


    Window(onCloseRequest = ::exitApplication,
            state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCES.ICON.FILENAME),
        title = "Chess"
    ) {
        val chess = remember { mutableStateOf(chessGame) }
        if (isAskingForName.value) {
            val gameName = remember { mutableStateOf("") }
            Dialog(
                onCloseRequest = { isAskingForName.value = false },
                title = "Insert Game Name",
            ) {
                Column{
                    Text("Please insert the name of the game to ${action.value}")
                    TextField(
                        value = gameName.value,
                        onValueChange = { gameName.value = it; chess.value.currentGameId = GameName(it) }
                    )
                    Button(
                        onClick = { isAskingForName.value = false },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
        menu(action,isAskingForName,chess )

        MaterialTheme {
            //val chess = remember { mutableStateOf(chessInfo)}
            val gameId = chess.value.currentGameId
            Row(Modifier.background(Color(ORANGE))) {
                Column(Modifier.padding(top = 32.dp)) {
                    for (i in 8 downTo 1) {
                        Text(
                            "$i",
                            fontSize = 21.sp,
                            modifier = Modifier.padding(top = 26.dp, bottom = 25.dp, start = 6.dp, end = 6.dp)
                        )
                    }
                }
                Column {
                    Row {
                        for (i in 0..7) {
                            Text(
                                "${(i + 'a'.code).toChar()}",
                                fontSize = 21.sp,
                                modifier = Modifier.padding(start = 32.dp, end = 31.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }
                    Box {
                        chessBoard(chess.value.board)
                    }
                    if(gameId != null){
                        Text(
                            "Game:${gameId.id} | You:${chess.value.currentPlayer} | ",
                            fontSize = 21.sp,
                            modifier = Modifier.padding(start = 4.dp, end = 31.dp, top = 32.dp, bottom = 4.dp)
                        )
                    }


                }
                Column(Modifier.padding(32.dp).height(640.dp).background(Color.White)) {
                    Text(
                        "TODO ON THE RIGHT",
                        fontSize = 21.sp,

                        )
                }

            }


        }
    }
}