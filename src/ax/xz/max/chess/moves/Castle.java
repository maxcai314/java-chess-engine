package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

import java.util.Set;

public enum Castle implements PlayerMove {
	WHITE_LONG(
			new Piece(Player.WHITE, PieceType.KING),
			new BoardCoordinate(0, 4), new BoardCoordinate(0, 2),
			new Piece(Player.WHITE, PieceType.ROOK), new BoardCoordinate(0, 0),
			new BoardCoordinate(0, 3),
			"O-O-O",
			Set.of(
					new BoardCoordinate(Player.WHITE.homeRank(), 2),
					new BoardCoordinate(Player.WHITE.homeRank(), 3)
			)
	),
	WHITE_SHORT(
			new Piece(Player.WHITE, PieceType.KING),
			new BoardCoordinate(0, 4),
			new BoardCoordinate(0, 6),
			new Piece(Player.WHITE, PieceType.ROOK),
			new BoardCoordinate(0, 7),
			new BoardCoordinate(0, 5),
			"O-O",
			Set.of(
					new BoardCoordinate(Player.WHITE.homeRank(), 5),
					new BoardCoordinate(Player.WHITE.homeRank(), 6)
			)
	),
	BLACK_LONG(
			new Piece(Player.BLACK, PieceType.KING),
			new BoardCoordinate(7, 4),
			new BoardCoordinate(7, 2),
			new Piece(Player.BLACK, PieceType.ROOK),
			new BoardCoordinate(7, 0),
			new BoardCoordinate(7, 3),
			"O-O-O",
			Set.of(
					new BoardCoordinate(Player.BLACK.homeRank(), 2),
					new BoardCoordinate(Player.BLACK.homeRank(), 3)
			)
	),
	BLACK_SHORT(
			new Piece(Player.BLACK, PieceType.KING),
			new BoardCoordinate(7, 4),
			new BoardCoordinate(7, 6),
			new Piece(Player.BLACK, PieceType.ROOK),
			new BoardCoordinate(7, 7),
			new BoardCoordinate(7, 5),
			"O-O",
			Set.of(
					new BoardCoordinate(Player.BLACK.homeRank(), 5),
					new BoardCoordinate(Player.BLACK.homeRank(), 6)
			)
	);

	private final Piece king;
	private final BoardCoordinate from;
	private final BoardCoordinate to;
	private final Piece rook;
	private final BoardCoordinate rookFrom;
	private final BoardCoordinate rookTo;

	private final String notation;

	private final Set<BoardCoordinate> clearanceSquares;

	Castle(Piece king, BoardCoordinate from, BoardCoordinate to, Piece rook, BoardCoordinate rookFrom, BoardCoordinate rookTo, String notation, Set<BoardCoordinate> clearanceSquares) {
		this.king = king;
		this.from = from;
		this.to = to;
		this.rook = rook;
		this.rookFrom = rookFrom;
		this.rookTo = rookTo;
		this.notation = notation;
		this.clearanceSquares = clearanceSquares;
	}

	public static Castle longCastle(Player player) {
		return switch (player) {
			case WHITE -> WHITE_LONG;
			case BLACK -> BLACK_LONG;
		};
	}

	public static Castle shortCastle(Player player) {
		return switch (player) {
			case WHITE -> WHITE_SHORT;
			case BLACK -> BLACK_SHORT;
		};
	}

	@Override
	public void execute(Board board) {
		board.placePiece(king, to);
		board.removePiece(from);
		board.placePiece(rook, rookTo);
		board.removePiece(rookFrom);
		board.revokeShortCastle(getPlayer());
		board.revokeLongCastle(getPlayer());

		board.switchTurn();

		board.incrementNumMoves();
		board.incrementHalfMoves(); // it's impossible for castles to be captures or pawn pushes
	}

	public Set<BoardCoordinate> getClearanceSquares() {
		return clearanceSquares;
	}

	@Override
	public Piece piece() {
		return king;
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
		return king.owner();
	}

	public Piece getRook() {
		return rook;
	}

	public BoardCoordinate getRookFrom() {
		return rookFrom;
	}

	public BoardCoordinate getRookTo() {
		return rookTo;
	}

	@Override
	public String toString() {
		return notation;
	}
}
