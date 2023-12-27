package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

public record RegularMove(Piece piece, BoardCoordinate from, BoardCoordinate to) implements PlayerMove {

	@Override
	public void execute(Board board) {
		if (board.hasCastlingRights(getPlayer()) && from.rank() == getPlayer().homeRank()) {
			if (piece.type() == PieceType.KING) {
				board.revokeShortCastle(getPlayer());
				board.revokeLongCastle(getPlayer());
			} else if (piece.type() == PieceType.ROOK) {
				if (from.file() == 0) {
					board.revokeLongCastle(getPlayer());
				} else if (from.file() == 7) {
					board.revokeShortCastle(getPlayer());
				}
			}
		}

		boolean isCapture = board.pieceAt(to) != null;
		boolean isPawnMove = piece.type() == PieceType.PAWN;

		board.placePiece(piece, to);
		board.removePiece(from);

		board.switchTurn();

		board.incrementNumMoves();
		if (isCapture || isPawnMove) board.resetHalfMoves();
		else board.incrementHalfMoves();
	}

	@Override
	public Player getPlayer() {
		return piece.owner();
	}

	@Override
	public String toString() {
		if (piece.type() == PieceType.PAWN) {
			if (to.file() == from.file()) {
				return String.format("%s%s", from, to);
			} else {
				return String.format("%sx%s", from, to);
			}
		}
		return String.format("%c%s%s", Character.toUpperCase(piece.toChar()), from, to);
	}
}
