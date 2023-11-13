public record PlayerMove(
        Piece piece,
        BoardCoordinate from,
        BoardCoordinate to
) { // todo: maybe make this a class that emits different types of moves
}
