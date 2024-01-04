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
			boolean fileAmbiguous = move.piece().type() == PieceType.PAWN ?
					isCapture() :
					legalMoves.stream()
							.filter(a -> a.piece().equals(move().piece()))
							.filter(a -> a.to().equals(move.to()))
							.count() > 1L;

			boolean rankAmbiguous = (move.piece().type() != PieceType.PAWN) && legalMoves.stream()
					.filter(a -> a.piece().equals(move().piece()))
					.filter(a -> a.to().equals(move.to()))
					.map(PlayerMove::from)
					.map(BoardCoordinate::file)
					.filter(file -> !fileAmbiguous || file == move.from().rank()) // only filter if file ambiguous
					.count() > 1L;

			if (move.piece().type() != PieceType.PAWN)
				sb.append(Character.toUpperCase(move().piece().type().toChar()));

			if (fileAmbiguous)
				sb.append(move().from().toString().charAt(0));
			if (rankAmbiguous)
				sb.append(move().from().toString().charAt(1));

			if (isCapture())
				sb.append("x");

			sb.append(move().to());

			if (move instanceof Promotion promotion)
				sb.append("=").append(promotion.getNewPiece().type().toChar());
		}

		if (isMate())
			sb.append("#");
		else if (isCheck())
			sb.append("+");

		return sb.toString();
	}
}
