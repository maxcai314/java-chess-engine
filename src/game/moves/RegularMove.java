package game.moves;

import game.*;

public final class RegularMove extends PlayerMove {
    private final Piece piece;
    private final BoardCoordinate from;
    private final BoardCoordinate to;

    public RegularMove(Piece piece, BoardCoordinate from, BoardCoordinate to) {
        this.piece = piece;
        this.from = from;
        this.to = to;
    }

    @Override
    public void execute(Board board) {
        if (board.hasCastlingRights(getPlayer()) && from.file() == getPlayer().homeRank()) {
            if (piece.type() == PieceType.KING) {
                board.revokeShortCastle(getPlayer());
                board.revokeLongCastle(getPlayer());
            }
            else if (piece.type() == PieceType.ROOK) {
                if (from.file() == 0) {
                    board.revokeLongCastle(getPlayer());
                }
                else if (from.file() == 7) {
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
    public boolean isPossible(Board board) {
        if (!from.isValid() || !to.isValid()) return false;

        if (piece == null) return false;
        if (!board.isEmpty(to) && board.pieceAt(to).owner() == getPlayer()) return false;
        if (!piece.equals(board.pieceAt(from))) return false;
        return true; //todo: add isInCheck to determine legality on a boardcopy
    }

    @Override
    public Piece getPiece() {
        return piece;
    }

    @Override
    public BoardCoordinate getFrom() {
        return from;
    }

    @Override
    public BoardCoordinate getTo() {
        return to;
    }

    @Override
    public Player getPlayer() {
        return piece.owner();
    }

    @Override
    public String toString() {
        if (piece.type() == PieceType.PAWN) {
            if (to.file() == from.file()){
                return String.format("%s%s", from, to);
            } else {
                return String.format("%sx%s", from, to);
            }
        }
        return String.format("%c%s%s", Character.toUpperCase(piece.toChar()), from, to);
    }
}
