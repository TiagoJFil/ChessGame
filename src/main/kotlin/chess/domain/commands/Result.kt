package chess.domain.commands

import chess.Chess

/**
 * Sealed class to represent the possible results of a command, it is sealed because we wont add more results.
 */
sealed class Result

/**
 * Result produced when the user makes an action.
 */
class CONTINUE(val chess: Chess) : Result()

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


