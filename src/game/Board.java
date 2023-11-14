package game;

import game.moves.*;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final Piece[][] DEFAULT_BOARD = new Piece[][] {
        { new Piece(Player.WHITE, PieceType.ROOK), new Piece(Player.WHITE, PieceType.KNIGHT), new Piece(Player.WHITE, PieceType.BISHOP), new Piece(Player.WHITE, PieceType.QUEEN), new Piece(Player.WHITE, PieceType.KING), new Piece(Player.WHITE, PieceType.BISHOP), new Piece(Player.WHITE, PieceType.KNIGHT), new Piece(Player.WHITE, PieceType.ROOK) },
        { new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN), new Piece(Player.WHITE, PieceType.PAWN) },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { null, null, null, null, null, null, null, null },
        { new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN), new Piece(Player.BLACK, PieceType.PAWN) },
        { new Piece(Player.BLACK, PieceType.ROOK), new Piece(Player.BLACK, PieceType.KNIGHT), new Piece(Player.BLACK, PieceType.BISHOP), new Piece(Player.BLACK, PieceType.QUEEN), new Piece(Player.BLACK, PieceType.KING), new Piece(Player.BLACK, PieceType.BISHOP), new Piece(Player.BLACK, PieceType.KNIGHT), new Piece(Player.BLACK, PieceType.ROOK) },
    };

    private final Piece[][] board;
    private Player currentTurn = Player.WHITE;
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

    public Piece getPiece(BoardCoordinate coordinate) {
        return board[coordinate.rank()][coordinate.file()];
    }

    public boolean isEmpty(BoardCoordinate coordinate) {
        return getPiece(coordinate) == null;
    }

    public boolean isLegalMove(PlayerMove move) {
        // if (move.piece().owner() != currentTurn) return false;
        // todo: implement
        return true;
    }

    public List<PlayerMove> getLegalMoves() {
        // todo: implement
        return null;
    }

    public void makeMove(PlayerMove move) {
        // todo: implement
    }

    public void undoMove() {
        // todo: implement
    }

    // todo: GameState
}
