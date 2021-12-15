package chess.domain

import Colors

/**
 * @param color      the color of the player
 * Represents a player in the game.
 */
class Player(val color : Colors){

    /**
     * @returns true if the player is white, false otherwise
     */
    fun isWhite() = this.color == Colors.WHITE

    /**
     * @return the opponent of this player
     */
    operator fun not(): Player {
        return when(this.color){
            Colors.BLACK -> Player(Colors.WHITE)
            Colors.WHITE -> Player(Colors.BLACK)
        }
    }
}

