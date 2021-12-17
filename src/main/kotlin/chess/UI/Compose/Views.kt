package chess.UI.Compose
import Bishop
import Board
import King
import Knight
import Pawn
import Piece
import Queen
import Rook
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.Key.Companion.Menu
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import chess.domain.Player
import chess.domain.board_components.toSquare
import org.junit.Test
import java.awt.TrayIcon

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
fun tile(piece: Piece?){
    var pieceImage = "empty-tile"
    if (piece != null) {
        val pieceColor = piece.player
        pieceImage = when (piece) {
            is Pawn -> "pawn.png"
            is Rook -> "rook.png"
            is Knight -> "knight.png"
            is Bishop -> "bishop.png"
            is Queen -> "queen.png"
            is King -> "king.png"
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
                modifier = Modifier.size(75.dp).align(Alignment.Center),
                // modifier = Modifier.size(60.dp).padding(5.dp),
                contentDescription = null
            )
        } else {
            Spacer(Modifier.size(75.dp))
        }
    }

}


@Composable
private fun BackgroundTile(tileColor: Colors) {
    val colorRGB =
        if (tileColor == Colors.BLACK) Color(0xFF789454)
        else Color(0xFFfcf1e8)

    Box(Modifier.background(colorRGB).size(75.dp)) {
    }
}


@Composable
fun chessBoard(board: Board) {
    buildBackgroundBoard()
    boardToComposable(board)

}

@Preview
@Composable
fun App(board: Board) {
    DesktopMaterialTheme {

        Box {
            Text("asd", fontSize = 20.sp , modifier = Modifier.padding(4.dp))

            chessBoard(board)

        }
        Row {

        }



    }
}
@Composable
fun makeMenu() = application {
    val action = remember { mutableStateOf("Last action: None") }
    val isOpen = remember { mutableStateOf(true) }

    if (isOpen.value) {

        Window(onCloseRequest = { isOpen.value = false }) {
            MenuBar {
                Menu("Game", mnemonic = 'G') {
                    Item("Open", onClick = { action.value = "Last action: OPEN" })
                    Item("Join", onClick = { action.value = "Last action: JOIN" })
                }
                Menu("Options", mnemonic = 'O') {
                    Item("Show Possible Moves", onClick = { action.value = "Clicked the other option" })
                }
            }
        }
    }
}
@Composable
fun showPossibleMoves(){

}
@Composable
fun FrameWindowScope.menu(action : MutableState<String>){
    MenuBar {
        Menu("Game", mnemonic = 'G') {
            Item("Open", onClick = { action.value = "Last action: OPEN" })
            Item("Join", onClick = { action.value = "Last action: JOIN" })
        }
        Menu("Options", mnemonic = 'O') {
            Item("Show Possible Moves", onClick = { action.value = "Clicked the other option" })
        }
    }
}

@Test
fun main() = application {
    val board = Board()
    val action = remember { mutableStateOf("Last action: None") }
    Window(onCloseRequest = ::exitApplication,
            state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource("favicon.ico"),
        title = "Chess"
    ) {
        menu(action)

        App(board)
    }
}