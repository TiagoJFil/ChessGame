package chess.domain

/**
 * @param color      the color of the player
 * Represents a player in the game.
 */
enum class Player{
    WHITE,
    BLACK;

    /**
     * @returns true if the player is white, false otherwise
     */
    fun isWhite() = this == WHITE

    /**
     * @return the opponent of this player
     */
    operator fun not(): Player {
        return when(this){
            Player.BLACK -> Player.WHITE
            Player.WHITE -> Player.BLACK
        }

    }

}