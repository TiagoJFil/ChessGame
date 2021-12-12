import chess.domain.board_components.Row
import org.junit.Assert.*
import org.junit.Test
/**
 * The Row type identifies one of the rows on the board, also called ranks.
 * Rows are identified by a digit from '8' to '1'. The top row is '8'.
 */
class TestRow {
    @Test
    fun `Digit to Row with ordinal property`() {
        val row = '7'.toRowOrNull()
        assertNotNull(row)
        assertEquals(1, row.ordinal)
    }
    @Test
    fun `Int to Row with digit property`() {
        val row = 3.toRow()
        assertEquals('5' ,row.digit)
    }
    @Test
    fun `Invalid digit to Row results null`() {
        val row = '0'.toRowOrNull()
        assertNull(row)
    }
    @Test
    fun `All valid digits to rows`() {
        assertEquals((7 downTo 0).toList(),  ('1'..'8').mapNotNull{ it.toRowOrNull()?.ordinal })
    }
    @Test
    fun `Get all valid values of Rows`() {
        assertEquals(8, Row.values().size)
        assertEquals(('1'..'8').toList().reversed(), Row.values().map{ it.digit })
    }
    @Test
    fun `All invalid rows`() {
        val invalidChars = (0..255).map{ it.toChar() } - ('1'..'8')
        val invalidRows = invalidChars.mapNotNull{ it.toRowOrNull() }
        assertEquals(0 , invalidRows.size)
    }
}