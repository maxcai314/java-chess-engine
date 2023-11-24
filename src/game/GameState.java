package game;

public enum GameState {
	UNFINISHED, WHITE_WON, BLACK_WON, DRAW;

	public static GameState ofWinner(Player winner) {
		return switch (winner) {
			case WHITE -> WHITE_WON;
			case BLACK -> BLACK_WON;
		};
	}
}
