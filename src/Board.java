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

    // castling rights (revoked when king or rook moves)
    private boolean whiteShortCastle = true;
    private boolean whiteLongCastle = true;
    private boolean blackShortCastle = true;
    private boolean blackLongCastle = true;

    public Board(Piece[][] board, Player currentTurn, ArrayList<PlayerMove> moves) {
        this.board = board;
        this.currentTurn = currentTurn;
        this.moves = moves;
    }

    public Board() {
        this(DEFAULT_BOARD, Player.WHITE, new ArrayList<PlayerMove>());
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
