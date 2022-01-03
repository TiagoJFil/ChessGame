package chess.domain.commands

import chess.Chess
import chess.Storage.Move

/**
 * Sealed class to represent the possible results of an action
 */
sealed class Result

/**
 * Result produced when the user makes an action.
 */
class CONTINUE(val chess: Chess, val moves: Iterable<Move>? ) : Result()

/**
 * Result produced when the user makes an error.
 */
class ERROR() : Result()

/**
 * Result produced a user makes a CHECK.
 */
class CHECK() : Result()

/**
 * Result produced a user makes a CHECKMATE.
 */
class CHECKMATE() : Result()
