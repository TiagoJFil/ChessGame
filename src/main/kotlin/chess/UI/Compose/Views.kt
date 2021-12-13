package chess.UI.Compose

import Bishop
import Board
import Colors
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
import chess.domain.board_components.toSquare
import org.junit.Test

@Preview
@Composable
fun buildBoard(){
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
fun boardToComposable(board: Board){
    Column {
        for(i in 8 downTo 1) {
                Row {
                    for (count in 0..7) {
                        val column = (count + 'a'.code).toChar()// convert to char
                        val square = "$column$i".toSquare()
                        val piece = board.getPieceAt(square)
                        tile(piece)
                    }
                }
        }
    }
}

@Composable
fun tile(piece: Piece?){
    var pieceImage = "empty-tile.png"
    if (piece != null) {
        val pieceColor = piece.player.color
        pieceImage = when (piece) {
            is Pawn -> "pawn.png"
            is Rook -> "rook.png"
            is Knight -> "knight.png"
            is Bishop -> "bishop.png"
            is Queen -> "queen.png"
            is King -> "king.png"
            else -> {"empty-tile.png"}
        }
        pieceImage = if (pieceColor == Colors.WHITE)
            "white-$pieceImage"
        else "black-$pieceImage"
    }
        Box{
            Image(
                painter = painterResource(pieceImage),
                modifier = Modifier.size(75.dp).align(Alignment.Center),
                // modifier = Modifier.size(60.dp).padding(5.dp),
                contentDescription = null)
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
        buildBoard()
        boardToComposable(board)
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