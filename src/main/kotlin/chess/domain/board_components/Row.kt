package chess.domain.board_components

import MAX_Y_NUMBER
import MIN_Y_NUMBER

/**
 * Represents the Row that is used to represent the Y axis on the board.
 * @property number    the number associated with the row
 */
enum class Row(val number: Int) {  //BoardMap[row][column]
    Eight(0),
    Seven(1),
    Six(2),
    Five(3),
    Four(4),
    Three(5),
    Two(6),
    One(7);

    fun value() = number

}

fun findRow(c : Char) : Row{  // '1' passa pra int e depois é que fazemos a operação
    require(c.isARow())
    return Row.values()[8 - c.toString().toInt()]
}
fun Int.toRow(): Row {
    require(this in MIN_Y_NUMBER..MAX_Y_NUMBER)
    return Row.values()[this]
}


fun Char.isARow() = this.toString().toInt() in MIN_Y_NUMBER..MAX_Y_NUMBER
