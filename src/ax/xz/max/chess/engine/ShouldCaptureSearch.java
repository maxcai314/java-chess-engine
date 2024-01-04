package ax.xz.max.chess.engine;

import ax.xz.max.chess.Board;
import ax.xz.max.chess.engine.choice.AlphaBetaSearch;
import ax.xz.max.chess.engine.choice.MovePicker;
import ax.xz.max.chess.engine.evaluators.ShannonEvaluator;
import ax.xz.max.chess.moves.PlayerMove;

public class ShouldCaptureSearch implements MovePicker {
	private final MovePicker onCapture;
	private final MovePicker onMove;

	public ShouldCaptureSearch(int captureDepth, int moveDepth) {
		this.onCapture = new AlphaBetaSearch(new ShannonEvaluator(), captureDepth);
		this.onMove = new AlphaBetaSearch(new ShannonEvaluator(), moveDepth);
	}

	public ShouldCaptureSearch(MovePicker onCapture, MovePicker onMove) {
		this.onCapture = onCapture;
		this.onMove = onMove;
	}

	@Override
	public PlayerMove chooseNextMove(Board board) {
		// todo: split the legal moves into captures and non-captures
		if (board.getLegalMoves().stream().anyMatch(playerMove -> board.copy().makeMove(playerMove).isCapture())) {
			return onCapture.chooseNextMove(board);
		} else {
			return onMove.chooseNextMove(board);
		}
	}
}
