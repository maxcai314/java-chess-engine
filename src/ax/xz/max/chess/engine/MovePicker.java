package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.moves.PlayerMove;

public interface MovePicker {
	PlayerMove chooseNextMove(Board board);
}
