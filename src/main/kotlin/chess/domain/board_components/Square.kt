package chess.domain.board_components

import BOARD_SIZE
import Direction


/**
 * Represents the Board position that uses columns and rows as coordinates.
 * @property column      the Column associated with the Square
 * @property row         the row associated with the Square
 * Only letters ranging from a to h and numbers from 1 to 8 are allowed.
 */
data class Square(val column: Column, val row: Row){
    override fun toString(): String {
        return "${column.letter}${BOARD_SIZE - row.number}"
    }
    fun addDirection(direction: Direction): Square? {
        if(column.number + direction.first < 0 || column.number + direction.first > 7)
            return null

        if(row.number + direction.second < 0 || row.number + direction.second > 7)
            return null
        return Square((column.number + direction.first).toColumn() , (row.number + direction.second).toRow())
    }

    fun addDirectionNotNull(direction: Direction): Square {
        return Square((column.number + direction.first).toColumn() , (row.number + direction.second).toRow())
    }

    fun toIndex(): Int = row.number * (BOARD_SIZE ) + column.number


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
 * Int extensions for expressing board coordinates
 */
fun Int.toSquare() : Square {

    return Square((this % BOARD_SIZE).toColumn(), (this / BOARD_SIZE).toRowOnList())
}

