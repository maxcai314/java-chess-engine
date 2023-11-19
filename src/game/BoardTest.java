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

    /*
        [SetUp "1"]
        [FEN "8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1"]
        1. Kc3 Rf3+ 2. Kd4 Kf5 3. Kd5 Rh3 4. Kd4 Rh4+ 5. Ke3 Re4+ 6. Kf3 Rf4+ 7. Kg3 Kg5
        8. Kh3 Rg4 9. Kh2 Kf4 10. Kh3 Kf3 11. Kh2 Rg3 12. a7 Ke2 13. Kh1 Kf2 14. Kh2 Ke1
        15. Kxg3 Ke2 16. Kf4 Kd3 17. a8=R *
     */
    @Test
    public void testMoveCount() {
        Board board = Board.fromFEN("8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1");
        System.out.println(board);
        System.out.println(board.toFEN());

        assertEquals(board.toFEN(), "8/5r2/P3k3/8/3K4/8/8/8 w - - 0 1");
        board.makeMove("Kc3");
        assertEquals(board.toFEN(), "8/5r2/P3k3/8/8/2K5/8/8 b - - 1 1");
        board.makeMove("Rf3+");
        assertEquals(board.toFEN(), "8/8/P3k3/8/8/2K2r2/8/8 w - - 2 2");
        board.makeMove("Kd4");
        assertEquals(board.toFEN(), "8/8/P3k3/8/3K4/5r2/8/8 b - - 3 2");
        board.makeMove("Kf5");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K4/5r2/8/8 w - - 4 3");
        board.makeMove("Kd5");
        assertEquals(board.toFEN(), "8/8/P7/3K1k2/8/5r2/8/8 b - - 5 3");
        board.makeMove("Rh3");
        assertEquals(board.toFEN(), "8/8/P7/3K1k2/8/7r/8/8 w - - 6 4");
        board.makeMove("Kd4");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K4/7r/8/8 b - - 7 4");
        board.makeMove("Rh4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/3K3r/8/8/8 w - - 8 5");
        board.makeMove("Ke3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/7r/4K3/8/8 b - - 9 5");
        board.makeMove("Re4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/4r3/4K3/8/8 w - - 10 6");
        board.makeMove("Kf3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/4r3/5K2/8/8 b - - 11 6");
        board.makeMove("Rf4+");
        assertEquals(board.toFEN(), "8/8/P7/5k2/5r2/5K2/8/8 w - - 12 7");
        board.makeMove("Kg3");
        assertEquals(board.toFEN(), "8/8/P7/5k2/5r2/6K1/8/8 b - - 13 7");
        board.makeMove("Kg5");
        assertEquals(board.toFEN(), "8/8/P7/6k1/5r2/6K1/8/8 w - - 14 8");
        board.makeMove("Kh3");
        assertEquals(board.toFEN(), "8/8/P7/6k1/5r2/7K/8/8 b - - 15 8");
        board.makeMove("Rg4");
        assertEquals(board.toFEN(), "8/8/P7/6k1/6r1/7K/8/8 w - - 16 9");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/8/P7/6k1/6r1/8/7K/8 b - - 17 9");
        board.makeMove("Kf4");
        assertEquals(board.toFEN(), "8/8/P7/8/5kr1/8/7K/8 w - - 18 10");
        board.makeMove("Kh3");
        assertEquals(board.toFEN(), "8/8/P7/8/5kr1/7K/8/8 b - - 19 10");
        board.makeMove("Kf3");
        assertEquals(board.toFEN(), "8/8/P7/8/6r1/5k1K/8/8 w - - 20 11");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/8/P7/8/6r1/5k2/7K/8 b - - 21 11");
        board.makeMove("Rg3");
        assertEquals(board.toFEN(), "8/8/P7/8/8/5kr1/7K/8 w - - 22 12");
        board.makeMove("a7");
        assertEquals(board.toFEN(), "8/P7/8/8/8/5kr1/7K/8 b - - 0 12");
        board.makeMove("Ke2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/4k2K/8 w - - 1 13");
        board.makeMove("Kh1");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/4k3/7K b - - 2 13");
        board.makeMove("Kf2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/5k2/7K w - - 3 14");
        board.makeMove("Kh2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/5k1K/8 b - - 4 14");
        board.makeMove("Ke1");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6r1/7K/4k3 w - - 5 15");
        board.makeMove("Kxg3");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6K1/8/4k3 b - - 0 15");
        board.makeMove("Ke2");
        assertEquals(board.toFEN(), "8/P7/8/8/8/6K1/4k3/8 w - - 1 16");
        board.makeMove("Kf4");
        assertEquals(board.toFEN(), "8/P7/8/8/5K2/8/4k3/8 b - - 2 16");
        board.makeMove("Kd3");
        assertEquals(board.toFEN(), "8/P7/8/8/5K2/3k4/8/8 w - - 3 17");
        board.makeMove("a8=R");
        System.out.println(board);
        assertEquals(board.toFEN(), "R7/8/8/8/5K2/3k4/8/8 b - - 0 17");

        System.out.println(board);
        System.out.println(board.toFEN());
    }

}