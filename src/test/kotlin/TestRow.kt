import chess.domain.board_components.*
import org.junit.Assert.*
import org.junit.Test
/**
 * The Row type identifies one of the rows on the board, also called ranks.
 * Rows are identified by a digit from '8' to '1'. The top row is '8'.
 */

class TestRow {
    @Test
    fun `Digit to Row with ordinal property`() {
        val row = '7'.toRow()
        assertNotNull(row)
        assertEquals(1, row.number)
    }
    @Test
    fun `Int to Row with digit property`() {
        val row = 3.toRow()
        assertEquals(Row.Five ,row)
    }

    @Test
    fun `Char to Row returns corresponding number`() {
        val row = '1'.toRow()
        assertEquals(7,row.number)
    }
    @Test
    fun `All valid digits to rows`() {
        assertEquals((7 downTo 0).toList(),  ('1'..'8').map{ it.toRow().ordinal })
    }
    @Test
    fun `Get all valid values of Rows`() {
        val values = Row.values().toList().reversed()
        val validValues = ('1'..'8').toList().map{it.toRow()}
        assertEquals(8, values.size)
        assertEquals(values,validValues)
    }

    @Test
    fun `Invalid char and int row`() {
       assertThrows(IllegalArgumentException::class.java){
           '9'.toRow()
       }
       assertThrows(IllegalArgumentException::class.java){
           9.toRow()
       }

    }

    @Test
    fun `All invalid char rows`() {
        for (i in ((Char.MIN_VALUE..Char.MAX_VALUE) - ('1'..'8')).toList())
            assertThrows(IllegalArgumentException::class.java) {
                i.toRow()
            }

    }

}








