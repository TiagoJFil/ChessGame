package chess.UI.Compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

/**
 * Makes the menu bar at the top of the screen
 * @param onClickOpen             The function to execute when the open button is clicked
 * @param onClickJoin             The function to execute when the join button is clicked
 * @param onClickShowMoves        The function to execute when the show moves button is clicked
 */
@Composable
fun FrameWindowScope.chessMenu(
    onClickOpen : () -> Unit,
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
            CheckboxItem("Show Possible Moves",checked = showMovesOption.value, onCheckedChange = {
                showMovesOption.value = it
                onClickShowMoves(showMovesOption.value)
            })
        }
    }
}