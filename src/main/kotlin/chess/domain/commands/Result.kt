package chess.domain.commands

import chess.Chess
import chess.domain.Move
import chess.domain.Player

/**
 * Sealed class to represent the possible results of an action
 */
sealed class Result

/**
 * Result produced when the user makes an action.
 */
class OK(val chess: Chess, val moves: Iterable<Move>?) : Result()

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