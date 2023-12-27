package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

public record EnPassant(
		Piece pawn,
		BoardCoordinate from,
		BoardCoordinate to,
		Piece capturedPawn,
		BoardCoordinate capturedPawnCoordinates
) implements PlayerMove {

	public static EnPassant enPassant(Player player, BoardCoordinate from, BoardCoordinate to) {
		return new EnPassant(new Piece(player, PieceType.PAWN), from, to, new Piece(player.opponent(), PieceType.PAWN), new BoardCoordinate(from.rank(), to.file()));
	}

	@Override
	public void execute(Board board) {
		board.placePiece(pawn, to);
		board.removePiece(from);
		board.removePiece(capturedPawnCoordinates);

		board.switchTurn();

		board.incrementNumMoves();
		board.resetHalfMoves(); // en passant is ALWAYS capture and pawn push
	}

	@Override
	public Piece piece() {
		return pawn;
	}

	@Override
	public BoardCoordinate from() {
		return from;
	}

	@Override
	public BoardCoordinate to() {
		return to;
	}

	@Override
	public Player getPlayer() {
		return pawn.owner();
	}

	public Piece getCapturedPawn() {
		return capturedPawn;
	}

	public BoardCoordinate getCapturedPawnCoordinates() {
		return capturedPawnCoordinates;
	}

	@Override
	public String toString() {
		return String.format("%sx%s", from, to);
	}
}
