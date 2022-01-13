package chess.UI.Compose

import chess.domain.Player

sealed class SHOWINFO();

class showCheckmate(val player: Player?) : SHOWINFO()
class showCheck(val player: Player?) : SHOWINFO()
class showStalemate : SHOWINFO()