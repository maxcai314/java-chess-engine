package ax.xz.max.chess.engine.choice;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.moves.PlayerMove;

import java.util.Set;

public class RandomMovePicker implements MovePicker {
	@Override
	public PlayerMove chooseNextMove(Board board) {
		Set<PlayerMove> moves = board.getLegalMoves();
		return moves.stream().skip((int) (Math.random() * moves.size())).findFirst().orElseThrow();
	}
}
