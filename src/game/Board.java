package game;

import game.moves.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Board {
    private static final Piece[][] DEFAULT_BOARD = new Piece[][] {
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
    private final ArrayList<PlayerMove> moves; // todo: make a moverecord

    // castling rights (revoked when king or rook game.moves)
    public boolean whiteShortCastle;
    public boolean whiteLongCastle;
    public boolean blackShortCastle;
    public boolean blackLongCastle;

    private int halfMoves; // for 50-move rule

    private int numMoves; // number of moves both players have made; divide by two to use

    private Board(Piece[][] board, Player currentTurn, ArrayList<PlayerMove> moves, boolean whiteShortCastle, boolean whiteLongCastle, boolean blackShortCastle, boolean blackLongCastle, int halfMoves, int numMoves) {
        this.board = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        this.currentTurn = currentTurn;
        this.moves = moves;
        this.whiteShortCastle = whiteShortCastle;
        this.whiteLongCastle = whiteLongCastle;
        this.blackShortCastle = blackShortCastle;
        this.blackLongCastle = blackLongCastle;
        this.halfMoves = halfMoves;
        this.numMoves = numMoves;
    }

    public Board() {
        this(DEFAULT_BOARD, Player.WHITE, new ArrayList<>(), true, true, true, true, 0, 0);
    }

    public void switchTurn() {
        currentTurn = currentTurn.opponent();
    }

    public void incrementHalfMoves() {
        halfMoves++;
    }

    public void resetHalfMoves() {
        halfMoves = 0;
    }

    public void incrementNumMoves() {
        numMoves++;
    }

    public Player currentTurn() {
        return currentTurn;
    }

    public static Board fromFEN(String text) {
        String[] words = text.split("\\s+");

        String boardString = words[0];
        String[] boardRows = boardString.split("/");
        Piece[][] board = new Piece[8][8];
        for (int row = 0; row < boardRows.length; row++) {
            String boardRow = boardRows[boardRows.length - row - 1];
            int column = 0;
            int index = 0;
            while (column < board[row].length) {
                char c = boardRow.charAt(index);
                if (Character.isDigit(c)) {
                    // number of spaces
                    int num = c - '0';
                    column += num;
                } else {
                    // piece
                    board[row][column] = Piece.fromChar(c);
                    column++;
                }
                index++;
            }
        }
        if (words.length == 1) {
            return new Board(board, Player.WHITE, new ArrayList<PlayerMove>(), true, true, true, true, 0, 0);
        }

        Player currentPlayer = switch (words[1].charAt(0)) {
            case 'w' -> {
                yield Player.WHITE;
            }
            case 'b' -> {
                yield Player.BLACK;
            } // todo: make Player.fromChar();
            default -> throw new IllegalArgumentException("Invalid Player FEN: " + text);
        };

        if (words.length == 2) {
            return new Board(board, currentPlayer, new ArrayList<PlayerMove>(), true, true, true, true, 0, 0);
        }

        String castlingRights = words[2];
        boolean whiteShortCastle = castlingRights.contains("K");
        boolean whiteLongCastle = castlingRights.contains("Q");
        boolean blackShortCastle = castlingRights.contains("k");
        boolean blackLongCastle = castlingRights.contains("q");

        if (words.length == 3) {
            return new Board(board, currentPlayer, new ArrayList<PlayerMove>(), whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, 0, 0);
        }

        String enPassant = words[3];
        ArrayList<PlayerMove> prevMoves = new ArrayList<>();
        if (!enPassant.equals("-")) {
            BoardCoordinate possibleCapture = BoardCoordinate.fromString(enPassant);
            // figure out the previous opponent move
            Player opponent = currentPlayer.opponent();
            Piece opponentPawn = new Piece(opponent, PieceType.PAWN);
            BoardCoordinate opponentPawnFrom = new BoardCoordinate(opponent.pawnRank(), possibleCapture.file());
            BoardCoordinate opponentPawnTo = opponentPawnFrom.step(2 * opponent.pawnDirection(), 0);
            BoardCoordinate capturedPawn = opponentPawnFrom.step(opponent.pawnDirection(), 0);
            if (capturedPawn.equals(possibleCapture))
                prevMoves.add(new RegularMove(opponentPawn, opponentPawnFrom, opponentPawnTo));
        }

        if (words.length < 5) {
            return new Board(board, currentPlayer, prevMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, 0, 0);
        }

        int halfMoves = Integer.parseInt(words[4]);
        int fullMoves = Integer.parseInt(words[5]);
        int numMoves = switch (currentPlayer) {
            case WHITE -> fullMoves * 2 - 2;
            case BLACK -> fullMoves * 2 - 1;
        };

        return new Board(board, currentPlayer, prevMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, halfMoves, numMoves
        );
    }

    public Board copy() {
        return new Board(board, currentTurn, new ArrayList<>(moves), whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, halfMoves, numMoves);
    }

    public String analysisLink() {
        String FEN = toFEN();
        // replace spaces in FEN with underscores
        FEN = FEN.replaceAll("\\s+", "_");
        return "https://lichess.org/analysis/" + FEN;
    }

    public Piece pieceAt(BoardCoordinate coordinate) {
        return board[coordinate.rank()][coordinate.file()];
    }

    public void placePiece(Piece piece, BoardCoordinate coordinate) {
        board[coordinate.rank()][coordinate.file()] = piece;
    }

    public void removePiece(BoardCoordinate coordinate) {
        board[coordinate.rank()][coordinate.file()] = null;
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

        String context;
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
                return new Promotion(piece, new Piece(currentTurn, PieceType.PAWN), new BoardCoordinate(currentTurn.opponent().pawnRank(), context.charAt(0) - 'a'), destination);
            } else {
                return new Promotion(piece, new Piece(currentTurn, PieceType.PAWN), new BoardCoordinate(currentTurn.opponent().pawnRank(), destination.file()), destination);
            }
        }

        if (context != null && context.length() == 1) {
            if ("abcdefgh".contains(context)) { // pawn captures
                int file = context.charAt(0) - 'a';
                Piece pawn = new Piece(currentTurn, PieceType.PAWN);

                // en passant if it exists
                if (!moves.isEmpty() && moves.getLast() instanceof RegularMove lastMove) {
                    if (lastMove.getPiece().type() == PieceType.PAWN && lastMove.getFrom().file() == destination.file()) {
                        BoardCoordinate pawnFrom = new BoardCoordinate(currentTurn.opponent().pawnRank(), destination.file());
                        BoardCoordinate capturedPawn = pawnFrom.step(currentTurn.opponent().pawnDirection(), 0);
                        BoardCoordinate pawnTo = pawnFrom.step(2 * currentTurn.opponent().pawnDirection(), 0);
                        if (lastMove.getFrom().equals(pawnFrom) &&
                            lastMove.getTo().equals(pawnTo) &&
                            isEmpty(capturedPawn) && isEmpty(pawnFrom) &&
                            !isEmpty(pawnTo) &&
                            pieceAt(pawnTo).equals(new Piece(currentTurn.opponent(), PieceType.PAWN)) &&
                            destination.equals(capturedPawn)
                        ) {
                            return EnPassant.enPassant(currentTurn, new BoardCoordinate(pawnTo.rank(), file), destination);
                        }
                    }
                }

                ArrayList<PlayerMove> candidates = new ArrayList<>(findAttacksOnCoordinate(pawn, destination));

                List<PlayerMove> possible = candidates.stream()
                        .filter(a -> a.getFrom().file() == file)
                        .toList(); // todo: find stream operation to findFirst, and throw if none or more than one

                if (possible.isEmpty()) throw new IllegalArgumentException("Invalid pawn position: " + text);
                else if (possible.size() == 1) return possible.getFirst();
                else throw new IllegalArgumentException("Ambiguous pawn capture: " + text);
            } else {
                // piece type specified
                // search for the piece that can move to the destination
                PieceType type = PieceType.fromChar(context.charAt(0));
                Piece piece = new Piece(currentTurn, type);
                List<PlayerMove> candidates = findAttacksOnCoordinate(piece, destination);
                if (candidates.isEmpty()) throw new IllegalArgumentException("Invalid piece position: " + text);
                else if (candidates.size() == 1) return candidates.getFirst();
                else throw new IllegalArgumentException("Invalid piece position: " + text);
            }
        } else if (context != null && context.length() == 2) { // piece with one axis specified
            PieceType type = PieceType.fromChar(context.charAt(0));
            Predicate<PlayerMove> filter = moveFilter(context);
            Piece piece = new Piece(currentTurn, type);
            List<PlayerMove> candidates = findAttacksOnCoordinate(piece, destination);
            List<PlayerMove> possible = candidates.stream().filter(filter).toList();
            if (possible.isEmpty()) throw new IllegalArgumentException("Invalid piece position: " + text);
            else if (possible.size() == 1) return possible.getFirst();
            else throw new IllegalArgumentException("Invalid piece position: " + text);
        } else if (context != null && context.length() == 3) { // piece with both axes specified
            PieceType type = PieceType.fromChar(context.charAt(0));
            Piece piece = new Piece(currentTurn, type);
            BoardCoordinate location = BoardCoordinate.fromString(context.substring(1));
            if (piece.equals(pieceAt(location))) return new RegularMove(piece, location, destination);
            else throw new IllegalArgumentException("Invalid piece position: " + text);
        } else { // don't use context, pawn pushes only
            Piece pawn = new Piece(currentTurn, PieceType.PAWN);
            BoardCoordinate singleStepFrom = destination.step(-1 * currentTurn.pawnDirection(), 0);
            BoardCoordinate doubleStepFrom = destination.step(-2 * currentTurn.pawnDirection(), 0);
            if (pawn.equals(pieceAt(singleStepFrom)))
                return new RegularMove(pawn, singleStepFrom, destination);
            else if (currentTurn.pawnRank() == doubleStepFrom.rank() && pieceAt(singleStepFrom) == null && pawn.equals(pieceAt(doubleStepFrom)))
                return new RegularMove(pawn, new BoardCoordinate(currentTurn.pawnRank(), destination.file()), destination);
            else throw new IllegalArgumentException("Invalid pawn position: " + text);
        }
    }

    private static Predicate<PlayerMove> moveFilter(String context) {
        Predicate<PlayerMove> filter;
        if ("abcdefgh".contains(context.substring(1))) { // rank specified
            int file = context.charAt(1) - 'a';
            filter = a -> a.getFrom().file() == file;
        } else if ("12345678".contains(context.substring(1))) { // file specified
            int rank = context.charAt(1) - '1';
            filter = a -> a.getFrom().rank() == rank;
        } else {
            throw new IllegalArgumentException("Invalid context string: " + context);
        }
        return filter;
    }

    private static final String ALGEBRAIC_REGEX_PATTERN = "([Oo0]-[Oo0](-[Oo0])?)|((([KQRBN]?[a-h]?[1-8]?x)|([KQRBN][a-h]?[1-8]?))?([a-h][1-8])(=[QRBN])?[+#]?)";

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
        Pattern pattern = Pattern.compile(ALGEBRAIC_REGEX_PATTERN);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid algebraic notation: " + text);

        String[] groups = new String[matcher.groupCount() + 1];
        for (int i = 0; i < groups.length; i++) {
            groups[i] = matcher.group(i);
        }

        return groups;
    }

    public boolean isEmpty(BoardCoordinate coordinate) {
        return pieceAt(coordinate) == null;
    }

    private static final List<BoardCoordinate> DIAGONAL_STEPS = List.of(
            new BoardCoordinate(1, 1),
            new BoardCoordinate(1, -1),
            new BoardCoordinate(-1, 1),
            new BoardCoordinate(-1, -1)
    );

    private static final List<BoardCoordinate> ORTHOGONAL_STEPS = List.of(
            new BoardCoordinate(1, 0),
            new BoardCoordinate(0, 1),
            new BoardCoordinate(-1, 0),
            new BoardCoordinate(0, -1)
    );

    private static final List<BoardCoordinate> ALL_STEPS = List.of(
            new BoardCoordinate(1, 1),
            new BoardCoordinate(1, 0),
            new BoardCoordinate(1, -1),
            new BoardCoordinate(0, 1),
            new BoardCoordinate(0, -1),
            new BoardCoordinate(-1, 1),
            new BoardCoordinate(-1, 0),
            new BoardCoordinate(-1, -1)
    );

    private static final List<BoardCoordinate> KNIGHT_STEPS = List.of(
            new BoardCoordinate(2, 1),
            new BoardCoordinate(2, -1),
            new BoardCoordinate(-2, 1),
            new BoardCoordinate(-2, -1),
            new BoardCoordinate(1, 2),
            new BoardCoordinate(1, -2),
            new BoardCoordinate(-1, 2),
            new BoardCoordinate(-1, -2)
    );

    /**
     * Gets the hypothetical moves a piece could make if it were of the input type and at the input position
     */
    private List<PlayerMove> attacksUsingPiece(Piece piece, BoardCoordinate position) {
        return switch (piece.type()) {
            case PAWN ->
                    Stream.of(1, -1)
                        .map(a -> position.step(piece.owner().pawnDirection(), a))
                        .filter(BoardCoordinate::isValid)
                        .filter(a -> !isEmpty(a) && pieceAt(a).owner() != piece.owner())
                        .flatMap(a ->
                        a.rank() == piece.owner().opponent().homeRank() ?
                            Stream.of(Promotion.allPromotions(piece, position, a)) :
                            Stream.of(new RegularMove(piece, position, a)
                        ))
                        .toList();

            case KNIGHT ->
                    KNIGHT_STEPS.stream()
                        .map(position::step)
                        .filter(BoardCoordinate::isValid)
                        .filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
                        .map(a -> (PlayerMove) new RegularMove(piece, position, a))
                        .toList();

            case KING ->
                    ALL_STEPS.stream()
                        .map(position::step)
                        .filter(BoardCoordinate::isValid)
                        .filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
                        .map(a -> (PlayerMove) new RegularMove(piece, position, a))
                        .toList();

            case BISHOP ->
                    DIAGONAL_STEPS.stream()
                        .flatMap(step -> {
                            ArrayList<BoardCoordinate> candidates = new ArrayList<>();
                                for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
                                    candidates.add(candidate);
                                    if (!isEmpty(candidate)) break;
                                }
                            return candidates.stream();
                        })
                        .filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
                        .map(a -> (PlayerMove) new RegularMove(piece, position, a))
                        .toList();

            case ROOK ->
                    ORTHOGONAL_STEPS.stream()
                        .flatMap(step -> {
                            ArrayList<BoardCoordinate> candidates = new ArrayList<>();
                            for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
                            candidates.add(candidate);
                            if (!isEmpty(candidate)) break;
                            }
                            return candidates.stream();
                        })
                        .filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
                        .map(a -> (PlayerMove) new RegularMove(piece, position, a))
                        .toList();

            case QUEEN ->
                    ALL_STEPS.stream()
                        .flatMap(step -> {
                            ArrayList<BoardCoordinate> candidates = new ArrayList<>();
                            for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
                                candidates.add(candidate);
                                if (!isEmpty(candidate)) break;
                            }
                            return candidates.stream();
                        })
                        .filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
                        .map(a -> (PlayerMove) new RegularMove(piece, position, a))
                        .toList();
        };
    }

    /**
     * Gets the hypothetical moves a piece could make from another square to attack the input position
     */
    private List<PlayerMove> findAttacksOnCoordinate(Piece piece, BoardCoordinate position) {
        Piece enemy = new Piece(piece.owner().opponent(), piece.type());
        return attacksUsingPiece(enemy, position)
                .stream()
                .filter(a -> a instanceof RegularMove) // promotions can't become attacks
                .filter(a -> piece.equals(pieceAt(a.getTo())))
                .map(a -> (PlayerMove) new RegularMove(piece, a.getTo(), a.getFrom())) // reverse move
                .toList();
    }

    private static final PieceType[] ATTACKING_PIECES = new PieceType[] {
            PieceType.PAWN,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.ROOK,
            PieceType.QUEEN,
            PieceType.KING
    };

    /**
     * Determines whether a given square is defended by the opponent
     */
    public boolean isDefendedBy(Player opponent, BoardCoordinate position) {
        // strategy: replace the position square with a piece of any type, and see if it attacks an opponent piece of the same type
        Player player = opponent.opponent();
        return Stream.of(ATTACKING_PIECES)
                .flatMap(pieceType -> attacksUsingPiece(new Piece(player, pieceType), position).stream())
                .anyMatch(move -> pieceAt(move.getTo()) != null && move.getPiece().type() == pieceAt(move.getTo()).type());
    }

    public boolean isInCheck(Player player) {
        // find king
        Piece king = new Piece(player, PieceType.KING);
        for (int rank = 0; rank < board.length; rank++) {
            for (int file = 0; file < board[rank].length; file++) {
                if (king.equals(board[rank][file])) {
                    return isDefendedBy(player.opponent(), new BoardCoordinate(rank, file));
                }
            }
        }
        throw new IllegalStateException("King not found");
    }

    public List<PlayerMove> getLegalMoves() {
        return getLegalMoves(currentTurn);
    }

    public List<PlayerMove> getLegalMoves(Player currentPlayer) {
        ArrayList<PlayerMove> legalMoves = new ArrayList<>();

        for (int rank = 0; rank < board.length; rank++) {
            for (int file = 0; file < board[rank].length; file++) {
                BoardCoordinate position = new BoardCoordinate(rank, file);
                Piece piece = pieceAt(position);

                if (piece == null || piece.owner() != currentPlayer)
                    continue;

                legalMoves.addAll(attacksUsingPiece(piece, position));

                // pawn pushes
                if (piece.type() == PieceType.PAWN) {
                    BoardCoordinate singleStepFrom = position.step(currentPlayer.pawnDirection(), 0);
                    if (isEmpty(singleStepFrom)) {
                        if (singleStepFrom.rank() == currentPlayer.opponent().homeRank()) {
                            legalMoves.addAll(List.of(Promotion.allPromotions(piece, position, singleStepFrom)));
                        } else {
                            legalMoves.add(new RegularMove(piece, position, singleStepFrom));

                            BoardCoordinate doubleStepFrom = singleStepFrom.step(currentPlayer.pawnDirection(), 0);
                            if (position.rank() == currentPlayer.pawnRank() && isEmpty(doubleStepFrom)) {
                                legalMoves.add(new RegularMove(piece, position, doubleStepFrom));
                            }
                        }
                    }
                }
            }
        }

        if (!isInCheck(currentPlayer) && canShortCastle(currentPlayer)) {
            ShortCastleSearch: {
                // check if the squares are defended
                for (int i = 5; i < 7; i++) {
                    BoardCoordinate coord = new BoardCoordinate(currentPlayer.homeRank(), i);
                    if (!isEmpty(coord)) {
                        break ShortCastleSearch;
                    }
                    if (isDefendedBy(currentPlayer.opponent(), coord)) {
                        break ShortCastleSearch;
                    }
                }
                legalMoves.add(Castle.shortCastle(currentPlayer));
            }
        }

        if (!isInCheck(currentPlayer) && canLongCastle(currentPlayer)) {
            LongCastleSearch: {
                // check if the squares are defended
                for (int i = 1; i < 4; i++) {
                    BoardCoordinate coord = new BoardCoordinate(currentPlayer.homeRank(), i);
                    if (!isEmpty(coord)) {
                        break LongCastleSearch;
                    }
                    if (isDefendedBy(currentPlayer.opponent(), coord)) {
                        break LongCastleSearch;
                    }
                }
                legalMoves.add(Castle.longCastle(currentPlayer));
            }
        }

        // en passant
        if (!moves.isEmpty() && moves.getLast() instanceof RegularMove lastMove) {
            if (lastMove.getPiece().type() == PieceType.PAWN) {
                BoardCoordinate pawnFrom = new BoardCoordinate(currentPlayer.opponent().pawnRank(), lastMove.getFrom().file());
                BoardCoordinate capturablePawn = pawnFrom.step(currentPlayer.opponent().pawnDirection(), 0);
                BoardCoordinate pawnTo = pawnFrom.step(2 * currentPlayer.opponent().pawnDirection(), 0);
                if (lastMove.getFrom().equals(pawnFrom) &&
                        lastMove.getTo().equals(pawnTo) &&
                        isEmpty(capturablePawn) && isEmpty(pawnFrom) &&
                        !isEmpty(pawnTo) &&
                        pieceAt(pawnTo).equals(new Piece(currentPlayer.opponent(), PieceType.PAWN))
                ) {
                    findAttacksOnCoordinate(new Piece(currentPlayer, PieceType.PAWN), capturablePawn)
                        .stream()
                        .map(PlayerMove::getFrom)
                        .map(from -> EnPassant.enPassant(currentPlayer, from, capturablePawn))
                        .forEach(legalMoves::add);
                }
            }
        }

        return legalMoves.stream()
                .filter(a -> a.isPossible(this))
                .filter(a -> {
                    Board copy = copy();
                    a.execute(copy);
                    return !copy.isInCheck(currentPlayer);
                })
                .toList();
    }

    public boolean hasCastlingRights(Player player) {
        return switch (player) {
            case WHITE -> whiteShortCastle || whiteLongCastle;
            case BLACK -> blackShortCastle || blackLongCastle;
        };
    }

    public boolean canLongCastle(Player player) {
        return switch (player) {
            case WHITE -> whiteLongCastle;
            case BLACK -> blackLongCastle;
        };
    }

    public boolean canShortCastle(Player player) {
        return switch (player) {
            case WHITE -> whiteShortCastle;
            case BLACK -> blackShortCastle;
        };
    }

    public void revokeLongCastle(Player player) {
        switch (player) {
            case WHITE -> whiteLongCastle = false;
            case BLACK -> blackLongCastle = false;
        }
    }

    public void revokeShortCastle(Player player) {
        switch (player) {
            case WHITE -> whiteShortCastle = false;
            case BLACK -> blackShortCastle = false;
        }
    }

    public void makeMove(PlayerMove move) {
        move.execute(this);
        moves.add(move);
    }

    public void makeMove(String notation) {
        makeMove(fromNotation(notation));
    }

    public GameState getState() {
        if (getLegalMoves().isEmpty()) {
            if (isInCheck(currentTurn)) {
                return GameState.ofWinner(currentTurn.opponent());
            } else {
                return GameState.DRAW;
            }
        }
        return GameState.UNFINISHED;
    }

    public String toFEN() {
        StringBuilder builder = new StringBuilder();
        for (int i= board.length-1;i>=0;i--) {
            int spaces = 0;
            for (Piece piece : board[i]) {
                if (piece == null) spaces++;
                else {
                    if (spaces > 0) {
                        builder.append(spaces);
                        spaces = 0;
                    }
                    builder.append(piece.toChar());
                }
            }
            if (spaces > 0) builder.append(spaces);
            builder.append("/");
        }
        builder.deleteCharAt(builder.length() - 1); // remove last slash
        builder.append(" ");

        builder.append(switch (currentTurn) {
            case WHITE -> "w";
            case BLACK -> "b";
        }).append(" ");

        if (whiteShortCastle) builder.append("K");
        if (whiteLongCastle) builder.append("Q");
        if (blackShortCastle) builder.append("k");
        if (blackLongCastle) builder.append("q");
        if (!whiteShortCastle && !whiteLongCastle && !blackShortCastle && !blackLongCastle) builder.append("-");

        builder.append(" ");

        EnPassant:
        {
            if (!moves.isEmpty()) {
                Player opponent = currentTurn.opponent();
                PlayerMove lastMove = moves.getLast();
                if (lastMove instanceof RegularMove regularMove) {
                    BoardCoordinate pawnFrom = new BoardCoordinate(opponent.pawnRank(), regularMove.getTo().file());
                    BoardCoordinate doubleStep = pawnFrom.step(2 * opponent.pawnDirection(), 0);
                    if (regularMove.getPiece().type() == PieceType.PAWN && regularMove.getFrom().equals(pawnFrom) && regularMove.getTo().equals(doubleStep)) {
                        builder.append(pawnFrom.step(opponent.pawnDirection(), 0)).append(" ");
                        break EnPassant;
                    }
                }
            }
            builder.append("- ");
        }

        builder.append(halfMoves)
                .append(" ")
                .append(numMoves / 2 + 1);

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Board other) {
            String thisFEN = this.toFEN();
            String otherFEN = other.toFEN();

            // compare FENs, but only look at first 3 words
            String[] thisWords = Arrays.stream(thisFEN.split("\\s+")).limit(3).toArray(String[]::new);
            String[] otherWords = Arrays.stream(otherFEN.split("\\s+")).limit(3).toArray(String[]::new);
            return Arrays.equals(thisWords, otherWords);
        }
        return false;
    }

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
