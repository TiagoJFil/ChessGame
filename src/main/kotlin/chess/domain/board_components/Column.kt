package chess.domain.board_components

import MAX_X_LETTER
import MIN_X_LETTER


/**
 * Represents the Column that is used to represent the X axis on the board.
 * @property letter    the letter associated with the Column
 */
enum class Column(val letter: Char) { //ordem normal
    A('a'),
    B('b'),
    C('c'),
    D('d'),
    E('e'),
    F('f'),
    G('g'),
    H('h');

    fun value() = letter - MIN_X_LETTER // ex: 'a' - 'a' == 0

}

/**
 * @return the Column associated with the given char value.
 */
fun Char.toColumn(): Column {
    val c = this.lowercaseChar()
    require(c.isAColumn()) {
        "Char is not valid"
    }
    val index = c - MIN_X_LETTER
    return Column.values()[index]
}


/**
 * @return the Column associated with the given int value.
 */
fun Int.toColumn() : Column  {
    require((MIN_X_LETTER +this).isAColumn()) {
        "Char is not valid"
    }
    return Column.values()[this]
}

/**
 * @return a boolean indicating if the given char is a valid Column.
 */
fun Char.isAColumn() = this in MIN_X_LETTER..MAX_X_LETTER
