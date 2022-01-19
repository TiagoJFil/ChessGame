package chess.ui


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import chess.GameName
import chess.domain.Player
import chess.toGameNameOrNull


private const val IMAGE_PROMOTION_SIZE = 50
private const val DIALOG_WIDTH= 400
private const val OPEN_GAME_DIALOG_HEIGHT = 200
private const val PROMOTION_DIALOG_HEIGHT = 150

/**
 * This function asks the user for a name and saves it in the [chess] object
 * @param actionName  a [String] that to displays the action
 * @param onClose    the function to execute when the dialog wants to be closed
 * @param onSubmit    the function to execute when someone clicks the submit button
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun openGameNameDialog(
    actionName : String,
    onClose : () -> Unit,
    onSubmit : (name : GameName) -> Unit
){
    val input = remember { mutableStateOf("") }

    val filterGameName = {
        val gameId = input.value.toGameNameOrNull()
        if(gameId != null){
            onSubmit(gameId)
        }else{
            input.value = ""
        }

    }
    Dialog(
        onCloseRequest = {onClose() },
        title = "Insert Game Name",
        resizable = false,
        state = DialogState(size = DpSize(width = DIALOG_WIDTH.dp, height = OPEN_GAME_DIALOG_HEIGHT.dp))

    ) {
        Column{
            Text(
                "Please insert the name of the game to $actionName",
                fontSize = 19.sp,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )
            TextField(
                value = input.value,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .onKeyEvent { event ->
                        if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                            filterGameName()
                        }
                        true
                    },
                onValueChange = { input.value = it }
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


/**
 * Creates a Dialog that gives the user 4 options to choose the promotion piece
 */
@Composable
@Preview
fun selectPossiblePromotions(
    currentPlayer: Player,
    onClose: () -> Unit,
    updateValue: (promotionPiece: String) -> Unit
) {

    val colorExtension = if(currentPlayer.isWhite()) "w_" else "b_"

    Dialog(
        onCloseRequest = { onClose() },
        title = "Promotion",
        state = DialogState(size = DpSize(DIALOG_WIDTH.dp, PROMOTION_DIALOG_HEIGHT.dp))
    ) {
        Column {

            Text(
                "Select promotion piece",
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally).absoluteOffset(x = 15.dp,15.dp),
                fontSize = 20.sp
            )

            Row(Modifier.padding(top = 1.dp).absoluteOffset(x = 25.dp, y = 10.dp)) {
                Button(
                    onClick = { updateValue("Q") },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val pieceFile = RESOURCE_QUEEN_FILENAME

                    Image(
                        painter = painterResource(resourcePath = "$colorExtension$pieceFile"),
                        contentDescription = pieceFile,
                        modifier = Modifier.size(IMAGE_PROMOTION_SIZE.dp, IMAGE_PROMOTION_SIZE.dp)
                    )

                }
                Button(
                    onClick = { updateValue("R") },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val pieceFile = RESOURCE_ROOK_FILENAME

                    Image(
                        painter = painterResource(resourcePath = "$colorExtension$pieceFile"),
                        contentDescription = pieceFile,
                        modifier = Modifier.size(IMAGE_PROMOTION_SIZE.dp, IMAGE_PROMOTION_SIZE.dp)
                    )
                }
                Button(
                    onClick = { updateValue("B") },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val pieceFile = RESOURCE_BISHOP_FILENAME

                    Image(
                        painter = painterResource(resourcePath = "$colorExtension$pieceFile"),
                        contentDescription = pieceFile,
                        modifier = Modifier.size(IMAGE_PROMOTION_SIZE.dp, IMAGE_PROMOTION_SIZE.dp)
                    )
                }
                Button(
                    onClick = { updateValue("N") },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val pieceFile = RESOURCE_KNIGHT_FILENAME

                    Image(
                        painter = painterResource(resourcePath = "$colorExtension$pieceFile"),
                        contentDescription = pieceFile,
                        modifier = Modifier.size(IMAGE_PROMOTION_SIZE.dp, IMAGE_PROMOTION_SIZE.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun showStalemateDialog(
    onClose : () -> Unit,
    onClickOpen : () -> Unit,
    onClickJoin : () -> Unit
){
    Dialog(
        onCloseRequest = {onClose() },
        title = "Game Over!",
        resizable = false,
        state = DialogState(size = DpSize(width = 400.dp, height = 200.dp))

    ) {
        Column{
            Text(
                "Game is a draw.\nPlease enter another game to play again",
                fontSize = 19.sp,
                modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
            )

            Button(
                onClick = { onClickOpen() },
                modifier = Modifier.absoluteOffset(150.dp,5.dp).size(100.dp,50.dp)
            ) {
                Text("Close")
            }
            Button(
                onClick = { onClickJoin() },
                modifier = Modifier.absoluteOffset(150.dp,5.dp).size(100.dp,50.dp)
            ) {
                Text("Open")
            }
            Button(
                onClick = { onClose() },
                modifier = Modifier.absoluteOffset(150.dp,5.dp).size(100.dp,50.dp)
            ) {
                Text("Join")
            }
        }
    }

}