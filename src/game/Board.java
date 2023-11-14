package game;

import game.moves.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Board {
    public static final Piece[][] DEFAULT_BOARD = new Piece[][] {
        { new Piece(Player.WHITE, PieceType.ROOK), new Piece(Player.WHITE, PieceType.KNIGHT), new Piece(Player.WHITE, PieceType.BISHOP), new Piece(Player.WHITE, PieceType.QUEEN), new Piece(Player.WHITE, PieceType.KING), new Piece(Player.WHITE, PieceType.BISHOP), new Piece(Player.WHITE, PieceType.KNIGHT), new Piece(Player.WHITE, PieceType.ROOK) },
        { new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN) },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN) },
        { new Piece(Player.BLACK, PieceType.ROOK), new Piece(Player.BLACK, PieceType.KNIGHT), new Piece(Player.BLACK, PieceType.BISHOP), new Piece(Player.BLACK, PieceType.QUEEN), new Piece(Player.BLACK, PieceType.KING), new Piece(Player.BLACK, PieceType.BISHOP), new Piece(Player.BLACK, PieceType.KNIGHT), new Piece(Player.BLACK, PieceType.ROOK) },
    };

    private final Piece[][] board;
    private Player currentTurn;
    private final ArrayList<PlayerMove> moves;

    // castling rights (revoked when king or rook game.moves)
    private boolean whiteShortCastle;
    private boolean whiteLongCastle;
    private boolean blackShortCastle;
    private boolean blackLongCastle;

    public Board(Piece[][] board, Player currentTurn, ArrayList<PlayerMove> moves, boolean whiteShortCastle, boolean whiteLongCastle, boolean blackShortCastle, boolean blackLongCastle) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.moves = moves;
        this.whiteShortCastle = whiteShortCastle;
        this.whiteLongCastle = whiteLongCastle;
        this.blackShortCastle = blackShortCastle;
        this.blackLongCastle = blackLongCastle;
    }

    public Board() {
        this(DEFAULT_BOARD, Player.WHITE, new ArrayList<PlayerMove>(), true, true, true, true);
    }

    public Board copy() {
        Piece[][] newBoard = new Piece[board.length][];
        for (int i = 0; i < board.length; i++) {
            newBoard[i] = board[i].clone();
        }
        return new Board(newBoard, currentTurn, new ArrayList<PlayerMove>(moves), whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle);
    }

    public Piece get(BoardCoordinate coordinate) {
        return board[coordinate.rank()][coordinate.file()];
    }

    public Piece getFromString(String coordinate) {
        return get(BoardCoordinate.fromString(coordinate));
    }

    public boolean isEmpty(BoardCoordinate coordinate) {
        return get(coordinate) == null;
    }

    public boolean isInCheck(Player player) {
        // find location of king
        BoardCoordinate kingLocation;
        search: {
            Piece king = new Piece(player, PieceType.KING);
            for (int rank = 0; rank < board.length; rank++) {
                for (int file = 0; file < board[rank].length; file++) {
                    Piece piece = board[rank][file];
                    if (king.equals(piece)) {
                        kingLocation = new BoardCoordinate(rank, file);
                        break search;
                    }
                }
            }
            return false;
        }

        return isDefended(kingLocation, player.opponent());
    }

    /**
     * Checks if the given king is defended by the given opponent.
     * @param location The square to check if defended
     * @param opponent The opponent to check for
     * @return Whether the given square is defended by the given opponent
     */
    private boolean isDefended(BoardCoordinate location, Player opponent) {
        // check for pawns
        Player player = opponent.opponent();
        switch (player) {
            case WHITE -> {
                // opponent checks from rank + 1
                Piece opponentPawn = new Piece(Player.BLACK, PieceType.PAWN);
                BoardCoordinate searchCoord = location.step(1, 1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;

                searchCoord = location.step(1, -1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;
            }
            case BLACK -> {
                // opponent checks from rank - 1
                Piece opponentPawn = new Piece(Player.WHITE, PieceType.PAWN);
                BoardCoordinate searchCoord = location.step(-1, 1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;

                searchCoord = location.step(-1, -1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;
            }
        }

        // check for knights
        for (int i = 1; i <= 2; i++) {
            int j = 3 - i;
            if (get(location.step(i, j)) != null && get(location.step(i, j)).equals(new Piece(opponent, PieceType.KNIGHT)))
                return true;
            if (get(location.step(i, -j)) != null && get(location.step(i, j)).equals(new Piece(opponent, PieceType.KNIGHT)))
                return true;
            if (get(location.step(-i, j)) != null && get(location.step(i, j)).equals(new Piece(opponent, PieceType.KNIGHT)))
                return true;
            if (get(location.step(-i, -j)) != null && get(location.step(i, j)).equals(new Piece(opponent, PieceType.KNIGHT)))
                return true;
        }

        // check diagonals for opponent bishops and queens
        if (checkOpponentPattern(
                location, 1, 1, opponent,
                Piece::attacksDiagonally
        )) return true;
        if (checkOpponentPattern(
                location, 1, -1, opponent,
                Piece::attacksDiagonally
        )) return true;
        if (checkOpponentPattern(
                location, -1, 1, opponent,
                Piece::attacksDiagonally
        )) return true;
        if (checkOpponentPattern(
                location, -1, -1, opponent,
                Piece::attacksDiagonally
        )) return true;

        // check ranks and files for opponent rooks and queens
        if (checkOpponentPattern(
                location, 1, 0, opponent,
                Piece::attacksOrthogonally
        )) return true;
        if (checkOpponentPattern(
                location, -1, 0, opponent,
                Piece::attacksOrthogonally
        )) return true;
        if (checkOpponentPattern(
                location, 0, 1, opponent,
                Piece::attacksOrthogonally
        )) return true;
        if (checkOpponentPattern(
                location, 0, -1, opponent,
                Piece::attacksOrthogonally
        )) return true;

        return false;
    }

    private boolean checkOpponentPattern(BoardCoordinate start, int rankStep, int fileStep, Player opponent, Predicate<Piece> pieceFilter) {
        BoardCoordinate searchCoord = start.step(rankStep, fileStep);
        while (searchCoord.isValid()) {
            if (get(searchCoord) != null && get(searchCoord).owner() == opponent && pieceFilter.test(get(searchCoord)))
                return true;

            searchCoord = searchCoord.step(rankStep, fileStep);
        }
        return false;
    }

    public boolean isLegalMove(PlayerMove move) {
        if (move.getPlayer() != currentTurn) return false;
        if (!move.isPossible(board)) return false;
        Board copy = copy();
        copy.makeMove(move);
        return copy.isInCheck(currentTurn); // if we are in check after our move, it is illegal
    }

    public List<PlayerMove> getLegalMoves() {
        // todo: implement
        return null;
    }

    private boolean hasCastlingRights(Player player) {
        return switch (player) {
            case WHITE -> whiteShortCastle || whiteLongCastle;
            case BLACK -> blackShortCastle || blackLongCastle;
        };
    }

    private void revokeLongCastle(Player player) {
        switch (player) {
            case WHITE -> whiteLongCastle = false;
            case BLACK -> blackLongCastle = false;
        }
    }

    private void revokeShortCastle(Player player) {
        switch (player) {
            case WHITE -> whiteShortCastle = false;
            case BLACK -> blackShortCastle = false;
        }
    }

    public void makeMove(PlayerMove move) {
        move.makeMove(board);
        moves.add(move);

        // revoke castling rights
        if (hasCastlingRights(currentTurn)) {
            Piece king = new Piece(currentTurn, PieceType.KING);
            Piece rook = new Piece(currentTurn, PieceType.ROOK);
            BoardCoordinate longCastleRook = new BoardCoordinate(currentTurn.homeRank(), 0);
            BoardCoordinate shortCastleRook = new BoardCoordinate(currentTurn.homeRank(), 7);
            if (king.equals(move.getPiece())) { // king moves, including castling
                revokeShortCastle(currentTurn);
                revokeLongCastle(currentTurn);
            } else if (rook.equals(move.getPiece())) { // individual rook moves
                if (move.getFrom().equals(longCastleRook)) revokeLongCastle(currentTurn);
                else if (move.getFrom().equals(shortCastleRook)) revokeShortCastle(currentTurn);
            }
        }

        currentTurn = currentTurn.opponent();
    }

    // todo: GameState

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i=board.length-1;i>=0;i--) { // flip board
            builder.append(i + 1).append("| ");
            for (Piece piece : board[i]) {
                builder.append(piece == null ? " " : piece.toChar()).append(" ");
            }
            builder.append("\n");
        }
        builder.append("  -----------------\n")
                .append("   a b c d e f g h\n");
        return builder.toString();
    }
}
