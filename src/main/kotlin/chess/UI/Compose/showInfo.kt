package chess.UI.Compose

import chess.domain.Player

sealed class ShowInfo();

class showCheckmate(val player: Player?) : ShowInfo()
class showCheck(val player: Player?) : ShowInfo()
class showStalemate : ShowInfo()