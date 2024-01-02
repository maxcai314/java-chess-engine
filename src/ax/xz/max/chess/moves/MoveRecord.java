package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

import java.util.Set;

/**
 * The dataclass of a move applied on a specific position.
 */
public record MoveRecord(
		BoardState prevBoard,
		PlayerMove move/*,
		GameState resultantState()*/ // todo: add resultant state
) {

	public BoardState resultantBoard() {
		return move.apply(prevBoard);
	}

	public Player player() {
		return move.getPlayer();
	}

	public boolean positionEquals(MoveRecord that) {
		return this.resultantBoard().equals(that.resultantBoard());
	}

	/**
	 * @return the estimated current state of the game, based on the current board state, without considering repetition
	 */
	public GameState resultantStateEstimate() {
		return resultantBoard().getCurrentState();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MoveRecord that)) return false;
		return this.positionEquals(that);
	}

	@Override
	public int hashCode() {
		return resultantBoard().hashCode();
	}

	public boolean gameEnded() {
		return resultantStateEstimate() != GameState.UNFINISHED; // todo: use better method resultantState var
	}

	public boolean isCapture() {
		if (move instanceof EnPassant) return true;
		else if (move instanceof Castle) return false; // unnecessary, but saves time
		else return !prevBoard().isEmpty(move.to());
	}

	public boolean isCheck() {
		return resultantBoard().isInCheck(player().opponent());
	}

	public boolean isMate() {
		var result = resultantBoard();
		return result.isInCheck(player().opponent())
				&& result.getLegalMoves().isEmpty()
				&& result.getCurrentState() == GameState.ofWinner(player()); // resultant state estimate will suffice
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (move instanceof Castle castle) {
			sb.append(castle); // we're not done, what if check/mate?
		} else { // we have to format like a normal move
			Set<PlayerMove> legalMoves = prevBoard().getLegalMoves();
			boolean rankAmbiguous = legalMoves.stream()
					.filter(a -> a.piece() == move().piece())
					.filter(a -> a.to() == move.to())
					.count() > 1L;

			boolean fileAmbiguous = (move.piece().type() == PieceType.PAWN && isCapture()) || legalMoves.stream()
					.filter(a -> a.piece() == move().piece())
					.filter(a -> a.to() == move.to())
					.map(PlayerMove::from)
					.map(BoardCoordinate::file)
					.filter(file -> !rankAmbiguous || file == move.from().file()) // only filter if rank ambiguous
					.count() > 1L;

			if (move.piece().type() != PieceType.PAWN)
				sb.append(Character.toUpperCase(move().piece().type().toChar()));

			if (rankAmbiguous)
				sb.append(move().from().toString().charAt(0));
			if (fileAmbiguous)
				sb.append(move().from().toString().charAt(1));

			if (isCapture())
				sb.append("x");

			sb.append(move().to());
		}

		if (isMate())
			sb.append("#");
		else if (isCheck())
			sb.append("+");

		return sb.toString();
	}
}
