package chess.ui

import chess.domain.Player

sealed class ShowInfo

/**
 * Show both players that there was a Checkmate and that given [loser] lost the game
 */
class ShowCheckmate(val loser: Player?) : ShowInfo()

/**
 * Shows the given [player] that he is currently in check
 */
class ShowCheck(val player: Player?) : ShowInfo()

/**
 * Show to both players that the game is a draw.
 */
object ShowStalemate : ShowInfo()