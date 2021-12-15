import chess.domain.board_components.Column
import chess.domain.board_components.toColumn
import org.junit.Assert.*
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.lang.IllegalArgumentException


/**
 * The Column type identifies one of the columns on the board, also called files.
 * Columns are identified by a letter from 'a' to 'h'. The left column is 'a'.
 */
class TestColumn {
    @Test
    fun `Letter to Column with ordinal property`() {
        val column = ('c').toColumn()
        assertNotNull(column)
        assertEquals(2, column.ordinal)
    }
    @Test
    fun `Int to Column with letter property`() {
        val column = 3.toColumn()
        assertEquals('d' ,column.letter)
    }
    @Test
    fun `Invalid letter to Column results null`() {
        assertThrows(IllegalArgumentException::class.java) {
            'x'.toColumn()
        }
    }
    @Test
    fun `Get all valid values of Column`() {
        assertEquals(8, Column.values().size)
        assertEquals(('a'..'h').toList(), Column.values().map{ it.letter })
    }
}