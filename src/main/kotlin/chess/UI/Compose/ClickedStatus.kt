package chess.UI.Compose

/**
 * Sealed class to represent the possible results of a command, it is sealed because we wont add more results.
 */
sealed class Clicked

/**
 * Information produced when the user has not clicked a tile.
 */
object NONE : Clicked()

/**
 * Information produced when the user clicked a tile for the first time.
 */
class START(val square: String) : Clicked()

/**
 * Information produced when the user clicked a tile for the second time.
 */
class FINISH(val square: String) : Clicked()
