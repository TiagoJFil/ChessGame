package chess.UI.Compose
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.Chess
import chess.UI.Compose.board.*
import chess.domain.board_components.Square
import chess.domain.commands.*
import kotlinx.coroutines.delay


private const val ORANGE = 0xFFB5651E

val TILE_SIZE = 60.dp
private val MOVES_TEXT_SIZE_HEIGHT = 500.dp
private val MOVES_TEXT_SIZE_WIDTH = 200.dp
private val MOVES_FONT_SIZE = 16.sp
private val INFO_FONT_SIZE = 18.sp
private val WINDOW_HEIGHT = 600.dp
private val BACKGROUND_COLOR = Color(ORANGE)
private val MOVES_BACKGROUND_COLOR = Color.White

const val RESOURCE_PAWN_FILENAME = "pawn.png"
const val RESOURCE_ROOK_FILENAME = "rook.png"
const val RESOURCE_KNIGHT_FILENAME = "knight.png"
const val RESOURCE_BISHOP_FILENAME = "bishop.png"
const val RESOURCE_QUEEN_FILENAME = "queen.png"
const val RESOURCE_KING_FILENAME = "king.png"
private const val RESOURCE_ICON_FILENAME = "favicon.ico"


enum class ACTION(val text: String) {
    OPEN("Open"),
    JOIN("Join")
}





/**
 * This is the main entry point for our application, as our app starts here.
 */
@Composable
fun ApplicationScope.App(chessInfo: Chess) {
    Window(
        onCloseRequest = ::exitApplication,
        state = WindowState(size = WindowSize(Dp.Unspecified, Dp.Unspecified)),
        icon = painterResource(RESOURCE_ICON_FILENAME),
        title = "Chess",
        resizable = false
    ) {
        //while this function is running , there will be coroutines running
        val coroutineScope = rememberCoroutineScope()
        val gameContent = remember { GameContentViews(chessInfo,GameActions(),coroutineScope) }


        chessMenu(
            onClickOpen = {
                gameContent.actionToEnterGame.value = ACTION.OPEN
            },
            onClickJoin = {
                gameContent.actionToEnterGame.value = ACTION.JOIN
            },
            onClickShowMoves = {
                gameContent.showPossibleMoves.value = it
            }
        )

        gameContent.clearPossibleMovesList()

        if(gameContent.actionToEnterGame.value != null){
            val action = gameContent.actionToEnterGame.value
            require(action != null)

            openGameNameDialog(
                action.text,
                onClose = { gameContent.actionToEnterGame.value = null },
                onSubmit = {
                    if (action == ACTION.JOIN) {
                        gameContent.joinAGame(it)
                    } else {
                        gameContent.openAGame(it)
                    }
                    gameContent.actionToEnterGame.value = null
                }
            )
        }

        if (gameContent.isSelectingPromotion.value) {

            selectPossiblePromotions(
                gameContent.chess.value.localPlayer,
                onClose = { gameContent.isSelectingPromotion.value = false }
            ) {   piece ->
                gameContent.updatePromotionValue(piece)
                gameContent.isSelectingPromotion.value = false
            }
        }


        LaunchedEffect(gameContent.chess.value) {
            while(true) {
                if(gameContent.chess.value.currentGameId != null && gameContent.chess.value.localPlayer != gameContent.chess.value.board.player) {
                    gameContent.refreshGame(this)
                }
                delay(1500)
            }
        }

        gameContent.actOnClickValue()



        // TODO QUANDO FAZEMOS PROMOTE E COMEMOS ALGUEM APARECE MAL NA DB
        // TODO QUANDO FAZEMOS CHECK E COMEMOS ALKGUEM APARECE MAL NA DB



        // TODO WHEN A LOT OF MOVES ARE PLAYED TO RIGHT MOVES  GO DOWN INTO INFINIY



        when(gameContent.getGameStatus()){
            is GameNotStarted ->{

                    drawVisualsWithoutAStartedGame()

            }
            is GameStarted ->{
                val infoToView = (gameContent.getGameStatus() as GameStarted).InfoToView

                     drawVisualsWithAStartedGame(
                         gameContent.chess.value,
                         infoToView,
                         gameContent.movesToDisplay.value,
                        checkIfTheTileIsAPossibleMove = { square ->
                            gameContent.isTileAPossibleMove(square)
                        },
                        checkIfTileIsSelected =   { square ->
                            gameContent.isTileSelected(square)
                        },
                        OnTileClicked = { square ->
                            gameContent.handleClick(square)
                        }
                    )

            }
            is GameOver ->{
                val infoToView = (gameContent.getGameStatus() as GameOver).InfoToView

                drawVisualsWithEndedGame(gameContent.chess.value,
                    infoToView,
                    gameContent.movesToDisplay.value
                )
            }
        }
    }
}




@Composable
private fun drawVisualsWithAStartedGame(
    chess: Chess,
    infoToShow: ShowInfo?,
    movesPlayed: String,
    checkIfTheTileIsAPossibleMove : (square: Square) -> Boolean,
    checkIfTileIsSelected: (square: Square) -> Boolean,
    OnTileClicked : (square: Square) -> Unit
) {
    val infoModifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp).background(Color.Red)
    val gameId = chess.currentGameId
    require(gameId != null) { "Game id is null" }
    MaterialTheme {

        Row(Modifier.background(BACKGROUND_COLOR).height(WINDOW_HEIGHT)) {

            drawCoordinateNumbers()

            Column{

                drawCoordinateLetters()

                Box {
                    buildBackgroundBoard()
                    boardToComposableView( chess.board, checkIfTheTileIsAPossibleMove, checkIfTileIsSelected, OnTileClicked)
                }

                val info = if (chess.board.player == chess.localPlayer) "Your turn" else "Waiting..."
                Text(
                    "Game:${gameId.id} | You:${chess.localPlayer} | $info",
                    fontSize = INFO_FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )

                when(infoToShow){
                    is showCheckmate-> {
                        val playerMessage = if (infoToShow.player == chess.localPlayer) "Lose." else "Win!!!"
                        Text(
                            "CHECKMATE You $playerMessage",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }
                    is showCheck -> {
                        if(infoToShow.player == chess.localPlayer) {
                            Text(
                                "CHECK",
                                fontSize = INFO_FONT_SIZE,
                                modifier = infoModifier
                            )
                        }
                    }
                    is showStalemate -> {
                        Text(
                            "STALEMATE",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }
                }
            }
            Column(
                Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH).background(MOVES_BACKGROUND_COLOR)
            ) {
                Text(
                    movesPlayed,
                    fontSize = MOVES_FONT_SIZE,
                )
            }
        }

    }
}


@Composable
private fun drawVisualsWithoutAStartedGame(){
    MaterialTheme {

        Row(Modifier.background(Color(ORANGE)).height(WINDOW_HEIGHT)) {

            drawCoordinateNumbers()

            Column{

                drawCoordinateLetters()

                Box {

                    buildBackgroundBoard()

                }
            }
            Column(
                Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH).background(Color.White)
            ) {
                //TODO: Add a text saying that the game has not started yet or maybe a button to start the game
            }
        }

    }
}

@Composable
private fun drawVisualsWithEndedGame(
    chess: Chess,
    infoToShow: ShowInfo?,
    movesPlayed: String,
){
    val infoModifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp).background(Color.Red)
    val gameId = chess.currentGameId
    require(gameId != null) { "Game id cant be null here" }

    MaterialTheme {

        Row(Modifier.background(BACKGROUND_COLOR).height(WINDOW_HEIGHT)) {

            drawCoordinateNumbers()

            Column {

                drawCoordinateLetters()

                Box {
                    buildBackgroundBoard()
                    boardToComposableView(chess.board, { false }, { false }, { })
                }

                val info = if (chess.board.player == chess.localPlayer) "Your turn" else "Waiting..."
                Text(
                    "Game:${gameId.id} | You:${chess.localPlayer} | $info",
                    fontSize = INFO_FONT_SIZE,
                    modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 16.dp)
                )

                when (infoToShow) {
                    is showCheckmate -> {
                        val playerMessage = if (infoToShow.player == chess.localPlayer) "Lose." else "Win!!!"
                        Text(
                            "CHECKMATE You $playerMessage",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }

                    is showStalemate -> {
                        Text(
                            "STALEMATE",
                            fontSize = INFO_FONT_SIZE,
                            modifier = infoModifier
                        )
                    }
                }
            }
            Column(
                Modifier.padding(32.dp).height(MOVES_TEXT_SIZE_HEIGHT).width(MOVES_TEXT_SIZE_WIDTH)
                    .background(MOVES_BACKGROUND_COLOR)
            ) {
                Text(
                    movesPlayed,
                    fontSize = MOVES_FONT_SIZE,
                )
            }
        }
    }


}