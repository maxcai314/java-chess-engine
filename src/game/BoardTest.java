package game;

import game.moves.EnPassant;
import game.moves.PlayerMove;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
    public static final String DEFAULT_BOARD =
            """
  _________________
8| r n b q k b n r |
7| p p p p p p p p |
6|                 |
5|                 |
4|                 |
3|                 |
2| P P P P P P P P |
1| R N B Q K B N R |
  -----------------
   a b c d e f g h
 """;

    @Test
    public void testToString() {
        Board board = new Board();
        assertEquals(board.toString(), DEFAULT_BOARD);
        System.out.println(board);
    }

    @Test
    public void testFromFEN() {
        Board board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - -");
        assertEquals(board.toString(), DEFAULT_BOARD);

        board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b -");
        assertEquals(board.toString(), DEFAULT_BOARD);

        System.out.print(board);
    }

    @Test
    public void testToFen() {
        String FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        System.out.println("Loading FEN: " + FEN);
        assertTrue(FEN.regionMatches(0, Board.fromFEN(FEN).toFEN(), 0, FEN.length()));
    }

    @Test
    public void testEnPassant() {
        Board board = new Board();
        board.makeMove("e4");
        board.makeMove("e6");
        board.makeMove("e5");
        board.makeMove("d5"); // en passant is possible on next move

        PlayerMove enPassant = board.fromNotation("exd6");
        assertTrue(enPassant.isPossible(board));
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("rnbqkbnr/ppp2ppp/4p3/3pP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6")); // important: FEN specifies possible en passant on d6

        board.makeMove(enPassant);
        System.out.println(board);
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("rnbqkbnr/ppp2ppp/3Pp3/8/8/8/PPPP1PPP/RNBQKBNR b KQkq -")); // important: FEN specifies no possible en passant
    }

    @Test
    public void testLoadEnPassant() {
        Board board = Board.fromFEN("4k3/8/4K3/6Pp/8/8/8/8 w - h6 0 2");
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("4k3/8/4K3/6Pp/8/8/8/8 w - h6")); // important: FEN specifies possible en passant on h6

        PlayerMove enPassant = board.fromNotation("gxh6");
        assertTrue(enPassant.isPossible(board));

        board.makeMove(enPassant);

        System.out.println(board);
        System.out.println(board.toFEN());
        assertTrue(board.toFEN().startsWith("4k3/8/4K2P/8/8/8/8/8 b - -")); // important: FEN specifies no possible en passant
    }

}