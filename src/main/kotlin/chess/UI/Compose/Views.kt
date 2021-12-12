package chess.UI.Compose

import Board
import Colors
import Piece
import Square
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import findColumn
import findRow
import org.junit.Test
import toSquare

@Preview
@Composable
fun BuildBoard(){
    Row {

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


@Composable
fun BoardToComposable(board: Board){
    Column {
        for(i in 8 downTo 1) {
                Row {
                    for (count in 0..7) {
                        val column = (count + 'a'.code).toChar()// convert to char
                        val square = "$column$i".toSquare()
                        val piece = board.getPieceAt(square)
                        Tile(piece)
                    }
                }
        }
    }
}

@Composable
fun Tile(piece: Piece?){

    if (piece != null) {
        val pieceColor = piece.player.color
        val pieceImage = when (piece.type) {
            PieceType.PAWN -> {
                if (pieceColor == Colors.WHITE)
                    "white-pawn.png"
                else
                    "black-pawn.png"
            }
            PieceType.ROOK -> {
                if (pieceColor == Colors.WHITE)
                    "white-rook.png"
                else
                    "black-rook.png"
            }
            PieceType.KNIGHT -> {
                if (pieceColor == Colors.WHITE)
                    "white-knight.png"
                else
                    "black-knight.png"
            }
            PieceType.BISHOP -> {
                if (pieceColor == Colors.WHITE)
                    "white-bishop.png"
                else
                    "black-bishop.png"
            }
            PieceType.QUEEN -> {
                if (pieceColor == Colors.WHITE)
                    "white-queen.png"
                else
                    "black-queen.png"
            }
            PieceType.KING -> {
                if (pieceColor == Colors.WHITE)
                    "white-king.png"
                else
                    "black-king.png"
            }
        }
        Box{
            Image(
                painter = painterResource(pieceImage),
                modifier = Modifier.size(75.dp).align(Alignment.Center),
                // modifier = Modifier.size(60.dp).padding(5.dp),
                contentDescription = null)
        }

    }
    if(piece == null){
        Box{
          Image(
              painter = painterResource("empty-tile.png"),
              modifier = Modifier.size(75.dp).align(Alignment.Center),
              contentDescription = null)
        }
    }

}


@Composable
fun BackgroundTile(tileColor: Colors) {
    val colorRGB =
        if (tileColor == Colors.BLACK) Color(0xFF789454)
        else Color(0xFFfcf1e8)

    Box(Modifier.background(colorRGB).size(75.dp)) {
    }
}



@Composable
@Preview
fun App(board: Board) {
    DesktopMaterialTheme {
        BuildBoard()
        BoardToComposable(board)
    }
}


@Test
fun main() = application {
    val board = Board()
    Window(onCloseRequest = ::exitApplication,
        state = WindowState(
            width =615.dp,
            height =635.dp,
        ),
        icon = painterResource("favicon.ico"),
        title = "Chess")
    {

        App(board)
    }
}