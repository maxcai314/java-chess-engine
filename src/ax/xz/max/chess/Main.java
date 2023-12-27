package ax.xz.max.chess;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.moves.PlayerMove;

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		Board board = new Board();
		System.out.println("2-player chess game");
		while (board.getState() == GameState.UNFINISHED) {
			System.out.println(board);
			PlayerMove move;
			while (true) {
				try {
					System.out.printf("It is currently %s's turn.%n", board.currentTurn());
					System.out.println("Enter your move in algebraic notation:");
					move = board.fromNotation(scanner.nextLine());
					break;
				} catch (IllegalArgumentException e) {
					System.out.println("\nThat's not a legal move. Please try again:");
				}
			}

			String moveName = board.makeMove(move).toString();
			System.out.printf("Making move %s:%n", moveName);
		}
		System.out.printf("%n%nEnd Result: %s%n", board.getState());
		System.out.println("Thanks for playing!");
	}
}