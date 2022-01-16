package chess.domain.board_components

import BOARD_SIZE
import MAX_ROW_NUMBER
import MAX_Y_NUMBER
import MIN_ROW_NUMBER
import MIN_Y_NUMBER

/**
 * Represents the Row that is used to represent the Y axis on the board.
 * @property number    the number associated with the row
 */
enum class Row(val number: Int) {
    Eight(0),
    Seven(1),
    Six(2),
    Five(3),
    Four(4),
    Three(5),
    Two(6),
    One(7);

    override fun toString(): String {
        return "${BOARD_SIZE - this.number}"
    }

}


fun Char.toRow() : Row{
    require(this.isARow())
    return Row.values()[8 - this.toString().toInt()]
}

fun Int.toRow(): Row {
    require(this in MIN_ROW_NUMBER..MAX_ROW_NUMBER)
    return Row.values()[this]
}

fun Int.toRowOnList(): Row {
    require(this in 0..7)
    return Row.values()[this]
}


private fun Char.isARow() = this.toString().toInt() in MIN_Y_NUMBER..MAX_Y_NUMBER
