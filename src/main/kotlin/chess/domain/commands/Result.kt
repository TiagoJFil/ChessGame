package chess.domain.commands
/**
 * Sealed class to represent the possible results of a command, it is sealed because we wont add more results.
 */
sealed class Result

/**
 * Result produced when the user makes an action.
 */
class CONTINUE<T>(val data: T) : Result()

/**
 * Result produced when the user wants to exit the program.
 */
class EXIT(val message: String) : Result()

/**
 * Result produced when the user makes an error.
 */
class ERROR(val message: String) : Result()

