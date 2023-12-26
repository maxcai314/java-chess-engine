package game.moves;

import game.*;

import java.util.Set;

public record MoveRecord(
		Board prevBoard,
		PlayerMove move
) {
	public MoveRecord {
		prevBoard = prevBoard.copy();
	}

	@Override
	public Board prevBoard() {
		return prevBoard.copy();
	}

	public Board resultantBoard() {
		Board board = prevBoard();
		board.makeMove(move());
		return board;
	}

	public Player player() {
		return move.getPlayer();
	}

	public boolean positionEquals(MoveRecord that) {
		return this.resultantBoard().equals(that.resultantBoard());
	}

	public GameState resultantState() {
		return resultantBoard().getState();
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
		return resultantState() != GameState.UNFINISHED;
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
		Board result = resultantBoard();
		return result.isInCheck(player().opponent()) && result.getLegalMoves().isEmpty();
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
					.map(PlayerMove::from)
					.map(BoardCoordinate::rank)
					.filter(((Integer) move().from().rank())::equals) // why does java need me to manually box
					.count() > 1L;

			boolean fileAmbiguous = legalMoves.stream()
					.filter(a -> a.piece() == move().piece())
					.map(PlayerMove::from)
					.map(BoardCoordinate::file)
					.filter(((Integer) move().from().file())::equals)
					.count() > 1L;

			if (move.piece().type() != PieceType.PAWN)
				sb.append(Character.toLowerCase(move().piece().type().toChar()));

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
