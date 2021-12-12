package chess.domain.board_components

import Direction
import MAX_Y_NUMBER
import MIN_Y_NUMBER


/**
 * Represents the Board position that uses columns and rows as coordinates.
 * @property column    the Column associated with the Square
 * @property row       the row associated with the Square
 * Only letters ranging from a to h and numbers from 1 to 8 are allowed.
 */
data class Square(val column: Column, val row: Row){
    override fun toString(): String {
        return "${column.letter}${MAX_Y_NUMBER - row.value()}"
    }
    fun addDirection(direction: Direction): Square? {
        if(this.column.value() + direction.first < 0 || this.column.value() + direction.first > 8)
            return null
        if(this.row.value() + direction.second < MIN_Y_NUMBER || this.row.value() + direction.second > MAX_Y_NUMBER)
            return null
        return Square((this.column.value() + direction.first).toColumn() , (this.row.value() + direction.second).toRow())
    }
}

/**
 * Convert a string into A Square Type
 */
fun String.toSquare(): Square {
    val column = this[0].lowercaseChar()
    val row = this[1].code.toChar()
    return Square(findColumn(column),findRow(row) )
}
