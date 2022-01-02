package chess.Storage

import chess.GameName
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import isel.leic.tds.storage.mongodb.*

class ChessGameAccessException(cause: Exception): Exception(cause)
private const val COLLECTION_NAME = "Games"
/**
 * Contract to be supported by the database using MongoDB storage
 */
interface DataBase {

    /**
     * Creates a new Collection with the given gameId if the collection does not exist
     * @param gameId      the id of the collection to be created
     * @return            a [Boolean] value indicating whether the collection was already exists (true) or if it created (false)
     */
    fun createGameDocumentIfItNotExists(gameId: GameName): Boolean

    /**
     * @param gameId     the id of the game whre we will put the move
     * @param move       the move to add to the dataBase
     * @return      a boolean value indicating whether the operation was successful (true) or not (false)
     */
    fun addMoveToDb(move: Move, gameId: GameName): Boolean

    /**
     * Gets the last movement played in the game
     * @param gameId     the id of the game where we will get the last move played from.
     */
    fun getLastMove(gameId: GameName): Move

    /**
     * @param gameId     the id of the game where we will get the move count from
     * @return           the number of moves played in the game
     */
    fun getMoveCount(gameId: GameName): Int

    /**
     * Gets the list of movement already played in the game
     * @param gameId     the id of the game where we will get the moves played from.
     * @return           an [Iterable] of [Move]s played in the game
     */
    fun getAllMoves(gameId: GameName): Iterable<Move>

    /**
     * @param gameId     the id of the game where we will check if it exists
     * @return           a [Boolean] value indicating whether the game exists (true) or not (false)
     */
    fun doesGameExist(gameId: GameName): Boolean
}

class ChessDataBase(private val db: MongoDatabase) : DataBase {

    /**
     * @param gameId     the id of the game whre we will put the move
     * @param move       the move to add to the dataBase
     * @return      a boolean value indicating whether the operation was successful (true) or not (false)
     */
    override fun addMoveToDb(move: Move, gameId: GameName): Boolean {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: return false
            val moves = doc.moves
            val newMoveOnDBList  = moves + move
            collection.updateDocument(Document(gameId.id,newMoveOnDBList))

         return true
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

    /**
     * Creates a new Collection with the given gameId if the collection does not exist
     * @param gameId      the id of the collection to be created
     * @return            a [Boolean] value indicating whether the collection was already exists (true) or if it created (false)
     */
    override fun createGameDocumentIfItNotExists(gameId: GameName): Boolean {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id)
            if(doc == null) {
                collection.createDocument(Document(gameId.id, listOf()))
                return false
            }
            return true
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

    /**
     * Gets the list of movement already played in the game
     * @param gameId     the id of the game where we will get the moves played from.
     * @return           an [Iterable] of [Move]s played in the game
     */
    override fun getAllMoves(gameId: GameName): Iterable<Move> {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())

            return  doc.moves.asIterable()
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

    /**
     * Gets the last movement played in the game
     * @param gameId     the id of the game where we will get the last move played from.
     */
    override fun getLastMove(gameId: GameName) : Move {
        try{
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())

            return  doc.moves.last()
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

    /**
     * @param gameId     the id of the game where we will get the move count from
     * @return           the number of moves played in the game
     */
    override fun getMoveCount(gameId: GameName): Int {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())

            return  doc.moves.size
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

    /**
     * @param gameId     the id of the game where we will check if it exists
     * @return           a [Boolean] value indicating whether the game exists (true) or not (false)
     */
    override fun doesGameExist(gameId: GameName): Boolean{
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            return (collection.getDocument(gameId.id) != null)
        } catch (e: MongoException) {
            throw ChessGameAccessException(e)
        }
    }

}

/**
 * Represents the information of a move that
 * @property moves    the move
 * @property _id      the id of the game where the move was played
 */
private data class Document(val _id: String, val moves: List<Move>)


/**
 * Represents a move in the game
 * @property move    the move
 * Only formatted moves are allowed ex: Pe2e4, pb1xc3, Kb6c7=Q , Pa6xb7=Q , etc.
 */
data class Move(val move: String){
    init {
           require(move.isFormatted())
    }
}

/**
 * Checks if a string is formatted correctly to be allowed as a moved
 * @return  a [Boolean] value indicating whether the string is formatted correctly (true) or not (false)
 */
private fun String.isFormatted(): Boolean {
    val filtered = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    return filtered.matches(this)
}