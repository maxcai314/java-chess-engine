package ax.xz.max.chess.engine.choice;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.moves.PlayerMove;

import java.util.Scanner;

public class HumanInput implements MovePicker {
	private final Scanner scanner = new Scanner(System.in);
	@Override
	public PlayerMove chooseNextMove(Board board) {
		while (true) {
			try {
				System.out.println("Enter your move: ");
				return board.fromNotation(scanner.nextLine());
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid move, try again");
				System.out.println(board);
			}
		}
	}
}
