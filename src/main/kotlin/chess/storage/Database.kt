package chess.storage

import chess.GameName
import chess.storage.MongoDb.createDocument
import chess.storage.MongoDb.getCollectionWithId
import chess.storage.MongoDb.getDocument
import chess.storage.MongoDb.updateDocument
import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase


class ChessDataBaseAccessException(cause: Exception): Exception(cause)
private const val DEFAULT_COLLECTION_NAME = "Games"

/**
 * Contract to be supported by the database using MongoDB storage
 */
private interface ChessDatabase {

    /**
     * Creates a new Collection with the given gameId if the collection does not exist
     * @param gameId      the id of the collection to be created
     * @return            a [Boolean] value indicating whether the creation was sucessfull (true) or not (false)
     */
    suspend fun createGameDocumentIfItNotExists(gameId: GameName): Boolean

    /**
     * @param gameId     the id of the game whre we will put the move
     * @param move       the move to add to the dataBase
     * @return      a boolean value indicating whether the operation was successful (true) or not (false)
     */
    suspend fun addMoveToDb(move: DatabaseMove, gameId: GameName): Boolean

    /**
     * Gets the last movement played in the game
     * @param gameId     the id of the game where we will get the last move played from.
     */
    suspend fun getLastMove(gameId: GameName): DatabaseMove?

    /**
     * @param gameId     the id of the game where we will get the move count from
     * @return           the number of moves played in the game
     */
    suspend fun getMoveCount(gameId: GameName): Int

    /**
     * Gets the list of movement already played in the game
     * @param gameId     the id of the game where we will get the moves played from.
     * @return           an [Iterable] of formated [DatabaseMove]s played in the game
     */
    suspend fun getAllMoves(gameId: GameName): Iterable<DatabaseMove>

    /**
     * @param gameId     the id of the game where we will check if it exists
     * @return           a [Boolean] value indicating whether the game exists (true) or not (false)
     */
    suspend fun doesGameExist(gameId: GameName): Boolean
}

/**
 * Implementation of the [ChessDatabase] contract using MongoDB
 */
class ChessRepository(private val db: MongoDatabase,private val COLLECTION_NAME : String = DEFAULT_COLLECTION_NAME) : ChessDatabase {

    /**
     * @param gameId     the id of the game whre we will put the move
     * @param move       the move to add to the dataBase
     * @return      a [Boolean] value indicating whether the operation was successful (true) or not (false)
     */
    override suspend fun addMoveToDb(move: DatabaseMove, gameId: GameName): Boolean {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: return false
            val moves = doc.moves
            val newMoveOnDBList  = moves + move
            collection.updateDocument(Document(gameId.id,newMoveOnDBList))

            return true
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }

    /**
     * Creates a new Collection with the given gameId if the collection does not exist
     * @param gameId      the id of the collection to be created
     * @return            a [Boolean] value indicating whether the creation was sucessfull (true) or not (false)
     */
    override suspend fun createGameDocumentIfItNotExists(gameId: GameName): Boolean {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id)
            if(doc == null) {
                collection.createDocument(Document(gameId.id, listOf()))
                return true
            }
            return false
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }



    /**
     * Gets the list of movement already played in the game
     * @param gameId     the id of the game where we will get the moves played from.
     * @return           an [Iterable] of [DatabaseMove]s played in the game
     */
    override suspend fun getAllMoves(gameId: GameName): Iterable<DatabaseMove> {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())

            return  doc.moves.asIterable()
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }

    /**
     * Gets the last movement played in the game
     * @param gameId     the id of the game where we will get the last move played from.
     */
    override suspend fun getLastMove(gameId: GameName) : DatabaseMove? {
        try{
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())
            val moveList = doc.moves
            return if(moveList.isEmpty()) null
            else moveList.last()
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }

    /**
     * @param gameId     the id of the game where we will get the move count from
     * @return           the number of moves played in the game
     */
    override suspend fun getMoveCount(gameId: GameName): Int {
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            val doc = collection.getDocument(gameId.id) ?: Document(gameId.id, listOf())

            return  doc.moves.size
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }

    /**
     * @param gameId     the id of the game where we will check if it exists
     * @return           a [Boolean] value indicating whether the game exists (true) or not (false)
     */
    override suspend fun doesGameExist(gameId: GameName): Boolean{
        try {
            val collection = db.getCollectionWithId<Document>(COLLECTION_NAME)
            return (collection.getDocument(gameId.id) != null)
        } catch (e: MongoException) {
            throw ChessDataBaseAccessException(e)
        }
    }

}

/**
 * Represents the information of a Document to be stored in the database
 * @property _id      the id of the game where the move will be stored
 * @property moves    the moves played in the game
 */
private data class Document(val _id: String, val moves: List<DatabaseMove>)

/**
 * Represents a move to be stored in the database
 */
data class DatabaseMove(val move: String){
    init {
        require(move.isADataBaseMove())
    }
    override fun toString(): String {
        return move
    }
}

/**
 * Validates if the given string is a valid DataBaseMove
 */
private fun String.isADataBaseMove() : Boolean{
    val rook = Regex("([Oo]-[Oo]?-?[Oo]?)")
    val filtered = Regex("([RNBQKPrnbqkp])([abcdefgh])([12345678])x?([abcdefgh])([12345678])=?([NBQR])?(.ep)?")
    return filtered.matches(this) || rook.matches(this)
}


fun Iterable<DatabaseMove>.getMovesAsString(): String {
    if (this.count() == 0) return ""
    var res = ""
    var plays = 0
    var noOfPlays = 1
    this.forEach {
        if (plays % 2 == 1) {
            noOfPlays++
            res+= "- ${it.move}\n"

        } else {
            res+= "$noOfPlays. ${it.move} "
        }
        plays++
    }
    return res

}