package ax.xz.max.chess.moves;

import ax.xz.max.chess.*;

public record RegularMove(Piece piece, BoardCoordinate from, BoardCoordinate to) implements PlayerMove {

	@Override
	public BoardState apply(BoardState board) {
		if (board.hasCastlingRights(getPlayer()) && from.rank() == getPlayer().homeRank()) {
			if (piece.type() == PieceType.KING) {
				board = board.revokeShortCastle(getPlayer());
				board = board.revokeLongCastle(getPlayer());
			} else if (piece.type() == PieceType.ROOK) {
				if (from.file() == 0) {
					board = board.revokeLongCastle(getPlayer());
				} else if (from.file() == 7) {
					board = board.revokeShortCastle(getPlayer());
				}
			}
		}

		boolean isCapture = board.pieceAt(to) != null;
		boolean isPawnMove = piece.type() == PieceType.PAWN;

		BoardCoordinate enPassantTarget = null;
		if (
				isPawnMove
				&& !isCapture
				&& from.rank() == getPlayer().pawnRank()
				&& to.rank() == getPlayer().pawnRank() + getPlayer().pawnDirection() * 2
		) {
			enPassantTarget = new BoardCoordinate(to.rank() - getPlayer().pawnDirection(), to.file());
		}

		return board
				.placePiece(piece, to)
				.removePiece(from)
				.prepareNextMove(isCapture || isPawnMove)
				.withEnPassantTarget(enPassantTarget);
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
