package ax.xz.max.chess;

public enum PieceType {
	PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

	public static PieceType fromChar(char c) {
		return switch (c) {
			case 'P' -> PAWN;
			case 'N' -> KNIGHT;
			case 'B' -> BISHOP;
			case 'R' -> ROOK;
			case 'Q' -> QUEEN;
			case 'K' -> KING;
			default -> throw new IllegalArgumentException("Invalid piece type character: " + c);
		};
	}

	public char toChar() {
		return switch (this) {
			case PAWN -> 'P';
			case KNIGHT -> 'N';
			case BISHOP -> 'B';
			case ROOK -> 'R';
			case QUEEN -> 'Q';
			case KING -> 'K';
		};
	}
}
