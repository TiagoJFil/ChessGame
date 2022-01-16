package chess.domain.commands

import chess.Chess
import chess.Storage.DatabaseMove
import chess.domain.Move
import chess.domain.Player

/**
 * Sealed class to represent the possible results of an action
 */
sealed class Result

/**
 * Result produced when the user makes an action and works as expected
 */
class OK(val chess: Chess, val moves: Iterable<DatabaseMove>?) : Result()

/**
 * Result produced when the user makes an error.
 */
class EMPTY() : Result()

/**
 * Result produced a user makes a CHECK.
 */
class CHECK(val chess: Chess,val playerInCheck: Player) : Result()

/**
 * Result produced a user makes a CHECKMATE.
 */
class CHECKMATE(val chess: Chess,val playerInCheckMate: Player) : Result()

/**
 * Result produced a user makes a STALEMATE.
 */
class STALEMATE(val chess: Chess) : Result()