package ax.xz.max.chess;

public enum Player {
	WHITE(0, 1, 1), BLACK(7, 6, -1);

	private final int homeRank;
	private final int pawnRank;
	private final int pawnDirection;

	Player(int homeRank, int pawnRank, int pawnDirection) {
		this.homeRank = homeRank;
		this.pawnRank = pawnRank;
		this.pawnDirection = pawnDirection;
	}

	public static Player fromChar(char c) {
		return switch (Character.toUpperCase(c)) {
			case 'W' -> WHITE;
			case 'B' -> BLACK;
			default -> throw new IllegalArgumentException("Invalid player character: " + c);
		};
	}

	public Player opponent() {
		return switch (this) {
			case WHITE -> BLACK;
			case BLACK -> WHITE;
		};
	}

	public int homeRank() {
		return homeRank;
	}

	public int pawnRank() {
		return pawnRank;
	}

	public int pawnDirection() {
		return pawnDirection;
	}
}
