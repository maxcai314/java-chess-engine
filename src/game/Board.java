package game;

import game.moves.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Player getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Converts algebraic notation into a PlayerMove
     * @param text The algebraic notation of the move as a string (e.g. "dxc4")
     * @return A PlayerMove
     */
    public PlayerMove fromNotation(String text) {
        String[] groups = parseAlgebraicNotation(text);
        if (groups[0] == null) throw new IllegalArgumentException("Invalid algebraic notation: " + text);
        if (groups[1] != null) { // castling
            if (groups[2] != null) return Castle.longCastle(currentTurn);
            else return Castle.shortCastle(currentTurn);
        }
        if (groups[3] == null) throw new IllegalArgumentException("Invalid algebraic notation: " + text);

        String context = null;
        if (groups[5] != null) { // capture
            // remove the 'x' at the end
            context = groups[5].substring(0, groups[5].length() - 1);
        } else { // if it's null, it's null anyways
            context = groups[6];
        }

        if (groups[7] == null) throw new IllegalArgumentException("Invalid algebraic notation: " + text);
        BoardCoordinate destination = BoardCoordinate.fromString(groups[7]);

        String promotion = groups[8];
        if (promotion != null) {
            PieceType type = PieceType.fromChar(promotion.charAt(1));
            Piece piece = new Piece(currentTurn, type);
            if (context != null) {
                return Promotion.fromFile(currentTurn, context.charAt(0) - 'a', destination.file(), piece);
            } else {
                return Promotion.fromFile(currentTurn, destination.file(), destination.file(), piece);
            }
        }

        if (context != null && context.length() == 1) {
            if ("abcdefgh".contains(context)) { // pawn
                Piece pawn = new Piece(currentTurn, PieceType.PAWN);
                List<BoardCoordinate> candidates = isDefendedFrom(destination, pawn);
                List<BoardCoordinate> possible = candidates.stream().filter(a -> a.file() == destination.file()).toList();
                if (possible.isEmpty()) throw new IllegalArgumentException("Invalid pawn position: " + text);
                else if (possible.size() == 1) return new RegularMove(pawn, possible.get(0), destination);
                else throw new IllegalArgumentException("Invalid pawn position: " + text);
            } else {
                // search for the piece that can move to the destination
                PieceType type = PieceType.fromChar(context.charAt(0));
                Piece piece = new Piece(currentTurn, type);
                List<BoardCoordinate> candidates = isDefendedFrom(destination, piece);
                if (candidates.isEmpty()) throw new IllegalArgumentException("Invalid piece position: " + text);
                else if (candidates.size() == 1) return new RegularMove(piece, candidates.get(0), destination);
                else throw new IllegalArgumentException("Invalid piece position: " + text);
            }
        } else if (context != null && context.length() == 2) { // piece with one axis specified
            PieceType type = PieceType.fromChar(context.charAt(0));
            Predicate<BoardCoordinate> filter = getBoardCoordinateFilter(context);
            Piece piece = new Piece(currentTurn, type);
            List<BoardCoordinate> candidates = isDefendedFrom(destination, piece);
            List<BoardCoordinate> possible = candidates.stream().filter(filter).toList();
            if (possible.isEmpty()) throw new IllegalArgumentException("Invalid piece position: " + text);
            else if (possible.size() == 1) return new RegularMove(piece, possible.get(0), destination);
            else throw new IllegalArgumentException("Invalid piece position: " + text);
        } else if (context != null && context.length() == 3) { // piece with both axes specified
            PieceType type = PieceType.fromChar(context.charAt(0));
            Piece piece = new Piece(currentTurn, type);
            BoardCoordinate location = BoardCoordinate.fromString(context.substring(1));
            if (piece.equals(get(location))) return new RegularMove(piece, location, destination);
            else throw new IllegalArgumentException("Invalid piece position: " + text);
        } else { // don't use context, pawn pushes only
            Piece pawn = new Piece(currentTurn, PieceType.PAWN);
            switch (currentTurn) {
                case WHITE -> {
                    if (pawn.equals(get(new BoardCoordinate(2, destination.file()))))
                        return new RegularMove(pawn, new BoardCoordinate(2, destination.file()), destination);
                    else if (pawn.equals(get(new BoardCoordinate(1, destination.file()))))
                        return new RegularMove(pawn, new BoardCoordinate(1, destination.file()), destination);
                    else throw new IllegalArgumentException("Invalid pawn position: " + text);
                }
                case BLACK -> {
                    if (pawn.equals(get(new BoardCoordinate(5, destination.file()))))
                        return new RegularMove(pawn, new BoardCoordinate(5, destination.file()), destination);
                    else if (pawn.equals(get(new BoardCoordinate(6, destination.file()))))
                        return new RegularMove(pawn, new BoardCoordinate(6, destination.file()), destination);
                    else throw new IllegalArgumentException("Invalid pawn position: " + text);
                }
            }
        }
        throw new IllegalArgumentException("Invalid algebraic notation: " + text); // temporary
    }

    private static Predicate<BoardCoordinate> getBoardCoordinateFilter(String context) {
        Predicate<BoardCoordinate> filter;
        if ("abcdefgh".contains(context.substring(1))) { // rank specified
            int file = context.charAt(1) - 'a';
            filter = a -> a.file() == file;
        } else if ("12345678".contains(context.substring(1))) { // file specified
            int rank = context.charAt(1) - '1';
            filter = a -> a.rank() == rank;
        } else {
            throw new IllegalArgumentException("Invalid context string: " + context);
        }
        return filter;
    }

    private static final String regex_pattern = "([Oo0]-[Oo0](-[Oo0])?)|((([KQRBN]?[a-h]?[1-8]?x)|([KQRBN][a-h]?[1-8]?))?([a-h][1-8])(=[QRBN])?[+#]?)";

    /**
     * Parses algebraic notation into an array of strings
     * @param text The algebraic notation of the move as a string (e.g. "cxd8=N+")
     * @return An array of strings formatted as follows:
     * <p> groups[0] = entire match
     * <p> groups[1] = castling match
     * <p> groups[2] = long castling
     * <p> groups[3] = normal move match
     * <p> groups[4] = capture/context match
     * <p> groups[5] = capture
     * <p> groups[6] = context
     * <p> groups[7] = destination
     * <p> groups[8] = promotion
     */
    private String[] parseAlgebraicNotation(String text) {
        Pattern pattern = Pattern.compile(regex_pattern);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid algebraic notation: " + text);

        String[] groups = new String[matcher.groupCount() + 1];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = matcher.group(i);
        }

        return groups;
    }

    public boolean isEmpty(BoardCoordinate coordinate) {
        return get(coordinate) == null;
    } // todo: replace == null and != null with this

    /**
     * Finds the square which contains the opponent piece that attacks/defends the given square.
     */
    private List<BoardCoordinate> isDefendedFrom(BoardCoordinate location, Piece piece) {
        ArrayList<BoardCoordinate> defendingSquares = new ArrayList<>();
        switch (piece.type()) {
            case PAWN -> {
                Player pieceOwner = piece.owner();
                switch (pieceOwner) {
                    case BLACK -> {
                        // black attacks from rank + 1
                        BoardCoordinate searchCoord = location.step(1, 1);
                        if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);

                        searchCoord = location.step(1, -1);
                        if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                    }
                    case WHITE -> {
                        // white attacks from rank - 1
                        BoardCoordinate searchCoord = location.step(-1, 1);
                        if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);

                        searchCoord = location.step(-1, -1);
                        if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                    }
                }
            }
            case KNIGHT -> {
                for (int i = 1; i <= 2; i++) {
                    int j = 3 - i;
                    if (location.step(i, j).isValid() && piece.equals(get(location.step(i, j))))
                        defendingSquares.add(location.step(i, j));
                    if (location.step(i, -j).isValid() && piece.equals(get(location.step(i, -j))))
                        defendingSquares.add(location.step(i, -j));
                    if (location.step(-i, j).isValid() && piece.equals(get(location.step(-i, j))))
                        defendingSquares.add(location.step(-i, j));
                    if (location.step(-i, -j).isValid() && piece.equals(get(location.step(-i, -j))))
                        defendingSquares.add(location.step(-i, -j));
                }
            }
            case BISHOP -> {
                BoardCoordinate coord = findDefendingSquare(location, 1, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 1, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
            }
            case ROOK -> {
                BoardCoordinate coord = findDefendingSquare(location, 1, 0, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, 0, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 0, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 0, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
            }
            case QUEEN -> {
                BoardCoordinate coord = findDefendingSquare(location, 1, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 1, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 1, 0, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, -1, 0, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 0, 1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
                coord = findDefendingSquare(location, 0, -1, piece.owner(), a -> a.equals(piece));
                if (coord != null) defendingSquares.add(coord);
            }
            case KING -> {
                BoardCoordinate searchCoord = location.step(1, 0);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(1, 1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(0, 1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(-1, 1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(-1, 0);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(-1, -1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(0, -1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
                searchCoord = location.step(1, -1);
                if (searchCoord.isValid() && piece.equals(get(searchCoord))) defendingSquares.add(searchCoord);
            }
        }
        return defendingSquares;
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
        Piece opponentPawn = new Piece(opponent, PieceType.PAWN);
        switch (opponent) {
            case BLACK -> {
                // opponent checks from rank + 1
                BoardCoordinate searchCoord = location.step(1, 1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;

                searchCoord = location.step(1, -1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;
            }
            case WHITE -> {
                // opponent checks from rank - 1
                BoardCoordinate searchCoord = location.step(-1, 1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;

                searchCoord = location.step(-1, -1);
                if (searchCoord.isValid() && opponentPawn.equals(get(searchCoord))) return true;
            }
        }

        // check for knights
        Piece opponentPiece = new Piece(opponent, PieceType.KNIGHT);
        for (int i = 1; i <= 2; i++) {
            int j = 3 - i;
            if (location.step(i, j).isValid() && opponentPiece.equals(get(location.step(i, j))))
                return true;
            if (location.step(i, -j).isValid() && opponentPiece.equals(get(location.step(i, -j))))
                return true;
            if (location.step(-i, j).isValid() && opponentPiece.equals(get(location.step(-i, j))))
                return true;
            if (location.step(-i, -j).isValid() && opponentPiece.equals(get(location.step(-i, -j))))
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
            if (get(searchCoord) != null)
                return get(searchCoord).owner() == opponent && pieceFilter.test(get(searchCoord));

            searchCoord = searchCoord.step(rankStep, fileStep);
        }
        return false;
    }

    private BoardCoordinate findDefendingSquare(BoardCoordinate start, int rankStep, int fileStep, Player opponent, Predicate<Piece> pieceFilter) {
        BoardCoordinate searchCoord = start.step(rankStep, fileStep);
        while (searchCoord.isValid()) {
            if (get(searchCoord) != null)
                return get(searchCoord).owner() == opponent && pieceFilter.test(get(searchCoord)) ? searchCoord : null;

            searchCoord = searchCoord.step(rankStep, fileStep);
        }
        return null;
    }

    public boolean isLegalMove(PlayerMove move) {
        if (move.getPlayer() != currentTurn) return false;
        if (!move.isPossible(board)) return false;
        Board copy = copy();
        copy.makeMove(move);
        return !copy.isInCheck(currentTurn); // if we are in check after our move, it is illegal
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
        builder.append("  _________________\n");
        for (int i=board.length-1;i>=0;i--) { // flip board
            builder.append(i + 1).append("| ");
            for (Piece piece : board[i]) {
                builder.append(piece == null ? " " : piece.toChar()).append(" ");
            }
            builder.append("|\n");
        }
        builder.append("  -----------------\n")
                .append("   a b c d e f g h\n");
        return builder.toString();
    }
}
