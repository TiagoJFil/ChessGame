package chess.ui.board

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import chess.domain.Player
import chess.domain.board_components.Square
import chess.ui.*

private val POSSIBLE_PATH_COLOR = Color.Green
private val POSSIBLE_PATH_SIZE= 30.dp
private const val POSSIBLE_PATH_OPACITY = 0.5f
private val TILE_SELECTED_COLOR = Color.Red

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
 * The Tile could be a piece, or a empty square and if [showPossibleMoves] option is active, also a circle.
 * @param piece                        The piece to draw on the tile, may be null(empty square).
 * @param checkIfTheTileIsAPossibleMove       Checks if the tile is a possible move for other piece.
 * @param checkIfTileIsSelected        Checks if the tile is selected by the user.
 * @param onSelected                   The function to call when the tile is clicked.
 */
@Composable
fun tile(
    piece: Piece?,
    square : Square,
    checkIfTheTileIsAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    onSelected: () -> Unit = { }
){
    val pieceImage = getResource(piece)
    val modifier = if(checkIfTileIsSelected(square)){
        Modifier
            .clickable( onClick = { onSelected() })
            .border(4.dp, TILE_SELECTED_COLOR)
    }
    else {
        Modifier
            .clickable( onClick = { onSelected() })
    }

    Box(modifier = modifier) {
        if (pieceImage != null) {
            Image(
                painter = painterResource(pieceImage),
                modifier = Modifier
                    .size(TILE_SIZE)
                    .align(Alignment.Center),
                contentDescription = null
            )

        } else {
            Spacer(modifier = Modifier.size(TILE_SIZE))
        }
        if (checkIfTheTileIsAPossibleMove(square))
            Box(modifier = Modifier
                .size(POSSIBLE_PATH_SIZE)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(POSSIBLE_PATH_COLOR.copy(alpha = POSSIBLE_PATH_OPACITY)) )

    }
}

/**
 * Builds a Background Tile of the chessboard with the [Color] received.
 * @param tileColor The color of the tile.
 */
@Composable
fun BackgroundTile(tileColor: Color) {
    Box(Modifier.background(tileColor).size(TILE_SIZE))
}