package chess.UI.Compose.board

import Board
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chess.domain.board_components.Square
import chess.domain.board_components.toSquare


private val BACKGROUND_GREEN_COLOR = Color(0xFF789454)
private val BACKGROUND_WHITE_COLOR = Color(0xFFfcf1e8)
private val MENU_HEIGHT = 27.dp
private val COORDINATES_FONT_SIZE = 21.sp
private val COORDINATES_BORDER_PADDING = 4.dp
private val PADDING_BETWEEN_NUMBERS = 18.dp
private val PADDING_BETWEEN_LETTERS = 24.dp

/**
 * Places the pieces from the [board] received on the visual chess board.
 * @param board                     The board to be displayed.
 * @param checkIfTheTileIsAPossibleMove    A function that checks if a square is a possible move.
 * @param checkIfTileIsSelected     A function that checks if a square is selected.
 * @param OnTileClicked             A function that is called when a tile is clicked.
 */
@Composable
fun boardToComposableView(
    board: Board,
    checkIfTheTileIsAPossibleMove : (square: Square) -> Boolean,
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

                    tile(piece, square, checkIfTheTileIsAPossibleMove, checkIfTileIsSelected){
                        OnTileClicked(square)
                    }
                }
            }
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
                            if (count % 2 == 0) BACKGROUND_WHITE_COLOR
                            else BACKGROUND_GREEN_COLOR
                        BackgroundTile(tileColor)
                    }
                }
            else{
                Column {
                    for (count in 1..8) {
                        val tileColor =
                            if (count % 2 == 1) BACKGROUND_WHITE_COLOR
                            else BACKGROUND_GREEN_COLOR
                        BackgroundTile(tileColor)
                    }
                }
            }
        }
    }
}

/**
 * Draws the chessboard Numbers.
 */
@Composable
fun drawCoordinateNumbers() {
    Column(Modifier.padding(top = MENU_HEIGHT)) {
        for (i in 8 downTo 1) {
            Text(
                "$i",
                fontSize = COORDINATES_FONT_SIZE,
                modifier = Modifier.padding(
                    top = PADDING_BETWEEN_NUMBERS,
                    bottom = PADDING_BETWEEN_NUMBERS,
                    start = COORDINATES_BORDER_PADDING,
                    end = COORDINATES_BORDER_PADDING
                )
            )

        }
    }
}

/**
 * Draws the chessboard Letters.
 */
@Composable
fun drawCoordinateLetters(){
    Row {
        for (i in 0..7) {
            Text(
                "${(i + 'a'.code).toChar()}",
                fontSize = COORDINATES_FONT_SIZE,
                modifier = Modifier.padding(
                    start = PADDING_BETWEEN_LETTERS,
                    end = PADDING_BETWEEN_LETTERS,
                    top = COORDINATES_BORDER_PADDING,
                    bottom = COORDINATES_BORDER_PADDING
                )
            )
        }
    }
}