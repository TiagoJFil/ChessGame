package chess.domain.board_components

import BOARD_SIZE
import Direction
import MAX_Y_NUMBER


/**
 * Represents the Board position that uses columns and rows as coordinates.
 * @property column      the Column associated with the Square
 * @property row         the row associated with the Square
 * Only letters ranging from a to h and numbers from 1 to 8 are allowed.
 */
data class Square(val column: Column, val row: Row){
    override fun toString(): String {
        return "${column.letter}${MAX_Y_NUMBER - row.value()}"
    }
    fun addDirection(direction: Direction): Square? {
        if(this.column.value() + direction.first < 0 || this.column.value() + direction.first > 7)
            return null

        if(this.row.value() + direction.second < 0 || this.row.value() + direction.second > 7)
            return null
        return Square((this.column.value() + direction.first).toColumn() , (this.row.value() + direction.second).toRow())
    }

    fun addDirectionNotNull(direction: Direction): Square {
        return Square((this.column.value() + direction.first).toColumn() , (this.row.value() + direction.second).toRow())
    }

    fun toIndex(): Int = this.row.value() * (BOARD_SIZE ) + this.column.value()



}

/**
 * Convert a string into a Square Type
 */
fun String.toSquare(): Square {
    val column = this[0].lowercaseChar().toColumn()
    val row = this[1].code.toChar().toRow()
    return Square(column,row )
}

/**
 * @param value        the value to be checked
 * Checks if a [value] is an index that may express a valid board coordinate
 */
fun isInCoordinateRange(value: Int) = value < BOARD_SIZE * BOARD_SIZE

/**
 * Int extensions for expressing board coordinates
 */
fun Int.toSquare() = Square((this / BOARD_SIZE).toColumn(), (this % BOARD_SIZE).toRow())
fun Int.toSquareOrNull() = if (isInCoordinateRange(this)) toSquare() else null
val Int.Square
    get(): Square = toSquare()

