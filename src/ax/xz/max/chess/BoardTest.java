package ax.xz.max.chess;

import ax.xz.max.chess.moves.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w");
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b");
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq");
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq");
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - -");
		assertEquals(DEFAULT_BOARD, board.toString());

		board = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b -");
		assertEquals(DEFAULT_BOARD, board.toString());

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
		System.out.println(board);

		board.makeMove("e4");
		board.makeMove("e6");
		board.makeMove("e5");
		board.makeMove("d5"); // en passant is possible on next move

		PlayerMove enPassant = board.fromNotation("exd6");
		assertTrue(board.getLegalMoves().contains(enPassant));
		System.out.println(board.toFEN());
		System.out.println("Lichess link: " + board.analysisLink());

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
		assertTrue(board.getLegalMoves().contains(enPassant));

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


	/*
		[SetUp "1"]
		[FEN "r3k3/8/8/8/8/8/8/R3K2R w KQq - 0 1"]
		1. Rb1 O-O-O 2. O-O *
	 */
	@Test
	public void testCastling() {
		Board board = Board.fromFEN("r3k3/8/8/8/8/8/8/R3K2R w KQq - 0 1");
		System.out.println(board);

		assertTrue(board.getLegalMoves(Player.WHITE).contains(Castle.shortCastle(Player.WHITE)));
		assertTrue(board.getLegalMoves(Player.WHITE).contains(Castle.longCastle(Player.WHITE)));

		board.makeMove("Rb1"); // revoke long castling privileges
		assertEquals(board.toFEN(), "r3k3/8/8/8/8/8/8/1R2K2R b Kq - 1 1");

		System.out.println(board);
		System.out.println("Legal Moves: " + board.getLegalMoves());

		assertTrue(board.getLegalMoves(Player.WHITE).contains(Castle.shortCastle(Player.WHITE)));
		assertFalse(board.getLegalMoves(Player.WHITE).contains(Castle.longCastle(Player.WHITE)));

		assertFalse(board.getLegalMoves(Player.BLACK).contains(Castle.shortCastle(Player.BLACK)));
		assertTrue(board.getLegalMoves(Player.BLACK).contains(Castle.longCastle(Player.BLACK)));

		board.makeMove("O-O-O");
		assertEquals(board.toFEN(), "2kr4/8/8/8/8/8/8/1R2K2R w K - 2 2");

		assertFalse(board.getLegalMoves(Player.BLACK).contains(Castle.shortCastle(Player.BLACK)));
		assertFalse(board.getLegalMoves(Player.BLACK).contains(Castle.longCastle(Player.BLACK)));

		board.makeMove("O-O");
		assertEquals(board.toFEN(), "2kr4/8/8/8/8/8/8/1R3RK1 b - - 3 2");

		assertFalse(board.getLegalMoves(Player.WHITE).contains(Castle.shortCastle(Player.WHITE)));
		assertFalse(board.getLegalMoves(Player.WHITE).contains(Castle.longCastle(Player.WHITE)));
	}

	@Test
	public void testIsLegal() {
		String FEN = "rnbqkbnr/ppp1pppp/8/3p4/3P1B2/8/PPP1PPPP/RN1QKBNR b KQkq - 1 2";
		Board board = Board.fromFEN(FEN);
		System.out.println("Loading FEN: " + board.toFEN());
		System.out.println(board);

		String[] opening = new String[] {
				"c5",
				"Nf3",
				"Nc6",
				"e3",
				"Bf5",
				"Nbd2",
				"e6",
				"c3",
				"Bd6",
				"Bg3",
				"Nf6",
				"Qb3",
				"O-O"
		}; // london system cope harder

		for (String move : opening) {
			System.out.println("\n\n");
			System.out.println(board.currentTurn() + ": " + move);
			PlayerMove playerMove = board.fromNotation(move);

			assertTrue(board.getLegalMoves().contains(playerMove));

			PlayerMove[] illegalMoves = new PlayerMove[] {
				new Promotion(new Piece(playerMove.getPlayer(), PieceType.KING), playerMove.piece(), playerMove.from(), playerMove.to()),
				new Promotion(new Piece(playerMove.getPlayer(), PieceType.ROOK), playerMove.piece(), playerMove.from(), playerMove.to()),
				Castle.longCastle(playerMove.getPlayer()),
				EnPassant.enPassant(playerMove.getPlayer(), BoardCoordinate.fromString("a1"), BoardCoordinate.fromString("a2")),
				new RegularMove(new Piece(playerMove.getPlayer(), PieceType.ROOK), BoardCoordinate.fromString("a1"), BoardCoordinate.fromString("a2"))
			};

			for (PlayerMove illegalMove : illegalMoves) {
				System.out.println("Testing illegal move: " + illegalMove);
				assertFalse(board.getLegalMoves().contains(illegalMove));
			}

			board.makeMove(playerMove);
			System.out.println(board);
			System.out.println("Position: " + board.toFEN());
		}
	}

	@Test
	public void testLegalMoves() {
		Board board = new Board();
		System.out.println(board);
		System.out.println(board.getLegalMoves());

		assertEquals(20, board.getLegalMoves().size());
		board.makeMove("e4");
		assertEquals(20, board.getLegalMoves().size());
		board.makeMove("e5");
		assertEquals(29, board.getLegalMoves().size());
		board.makeMove("f4");
		assertEquals(30, board.getLegalMoves().size());
		board.makeMove("exf4");
		assertEquals(29, board.getLegalMoves().size());
		board.makeMove("Nf3");
		assertEquals(29, board.getLegalMoves().size());
		board.makeMove("Nc6");
		assertEquals(29, board.getLegalMoves().size());
		board.makeMove("Bb5");
		assertEquals(31, board.getLegalMoves().size());
		board.makeMove("d6");
		assertEquals(34, board.getLegalMoves().size()); // including castle
		board.makeMove("d3");
		assertEquals(27, board.getLegalMoves().size()); // black's knight is pinned
		board.makeMove("Qh4+"); // check
		assertEquals(5, board.getLegalMoves().size()); // can't castle while in check
		board.makeMove("Nxh4");
		assertEquals(24, board.getLegalMoves().size()); // knight is still pinned
		board.makeMove("Bd7");
		assertEquals(36, board.getLegalMoves().size());
		board.makeMove("Bxf4");
		assertEquals(32, board.getLegalMoves().size()); // knight is unpinned
		board.makeMove("h6");
		assertEquals(42, board.getLegalMoves().size());
		board.makeMove("Bg5");
		assertEquals(28, board.getLegalMoves().size()); // castle is blocked

		for (PlayerMove move : board.getLegalMoves()) {
			assertTrue(board.getLegalMoves().contains(move));
		}

		System.out.println(board);


		Board endgame = Board.fromFEN("8/5r1p/2k5/6PR/3K4/8/8/8 w - - 0 1");
		System.out.println(endgame);

		assertEquals(13, endgame.getLegalMoves().size()); // king cuts off other king's moves
		endgame.makeMove("Rh4");
		assertEquals(21, endgame.getLegalMoves().size());
		endgame.makeMove("h5");
		assertEquals(15, endgame.getLegalMoves().size()); // en passant
		endgame.makeMove("gxh6");
		assertEquals(20, endgame.getLegalMoves().size());
		endgame.makeMove("Rd7+");
		assertEquals(5, endgame.getLegalMoves().size()); // escape check
		endgame.makeMove("Ke3");
		assertEquals(21, endgame.getLegalMoves().size());
		endgame.makeMove("Re7+");
		assertEquals(7, endgame.getLegalMoves().size()); // rook can also block check
		endgame.makeMove("Re4");
		assertEquals(19, endgame.getLegalMoves().size());
		endgame.makeMove("Rf7");
		assertEquals(16, endgame.getLegalMoves().size());
		endgame.makeMove("Rh4");
		assertEquals(22, endgame.getLegalMoves().size());
		endgame.makeMove("Rf8");
		assertEquals(17, endgame.getLegalMoves().size());
		endgame.makeMove("h7");
		assertEquals(22, endgame.getLegalMoves().size());
		endgame.makeMove("Rg8");
		assertEquals(28, endgame.getLegalMoves().size()); // two promotion squares
		endgame.makeMove("hxg8=Q");
		assertEquals(7, endgame.getLegalMoves().size());
		endgame.makeMove("Kb7");
		assertEquals(43, endgame.getLegalMoves().size());
		endgame.makeMove("Qg7+");
		assertEquals(6, endgame.getLegalMoves().size());
		endgame.makeMove("Kc8");
		assertEquals(45, endgame.getLegalMoves().size());
		endgame.makeMove("Rh8#");

		assertEquals(0, endgame.getLegalMoves().size()); // checkmate
		assertTrue(endgame.isInCheck(Player.BLACK));

		System.out.println(endgame);
		System.out.println("Checkmate!");
		System.out.println("Position: " + endgame.toFEN());
		System.out.println("Permalink to analysis: " + endgame.analysisLink());
	}

	@Test
	public void testBoardCopy() {
		Board board1 = new Board();
		Board board2 = new Board();
		Board board3 = board1.copy();
		Board board4 = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 10 10"); // identical
		Board board5 = Board.fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1"); // no castling rights

		assertNotSame(board1, board2);
		assertNotSame(board1, board3);
		assertNotSame(board2, board3);
		assertNotSame(board1, board4);
		assertNotSame(board1, board5);

		assertEquals(board1, board2);
		assertEquals(board1, board3);
		assertEquals(board2, board3);
		assertEquals(board1, board4);

		assertNotEquals(board1, board5);
	}

	@Test
	public void testGameState() {
		Board board = new Board();

		System.out.println(board);
		System.out.println("\n\n");

		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("e4");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("d5");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("Nf3");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("dxe4");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("Ne5");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("f6");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("Qh5+");
		assertEquals(GameState.UNFINISHED, board.gameState());

		assertTrue(board.isInCheck(Player.BLACK));
		assertFalse(board.isInCheck(Player.WHITE));

		System.out.println(board);
		System.out.println("Check!");
		System.out.println("Position: " + board.toFEN());
		System.out.println("\n\n");

		board.makeMove("g6");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("Bc4");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("gxh5");
		assertEquals(GameState.UNFINISHED, board.gameState());
		board.makeMove("Bf7#");
		assertEquals(GameState.WHITE_WON, board.gameState());

		System.out.println(board);
		System.out.println("Checkmate!");
		System.out.println("Position: " + board.toFEN());
		System.out.println("Permalink to analysis: " + board.analysisLink());
		System.out.println("\n\n");

		Board endgame = Board.fromFEN("7k/5K2/8/8/8/8/6Q1/8 w - - 0 1");
		System.out.println("Endgame Analysis:");
		System.out.println(endgame);
		System.out.println("\n\n");

		Map<String, GameState> moves = new HashMap<>();
		moves.put("Qg8#", GameState.WHITE_WON);
		moves.put("Qg7#", GameState.WHITE_WON);
		moves.put("Qg6", GameState.DRAW);
		moves.put("Qg5", GameState.UNFINISHED);
		moves.put("Qh1#", GameState.WHITE_WON);
		moves.put("Qh2#", GameState.WHITE_WON);
		moves.put("Qh3#", GameState.WHITE_WON);
		moves.put("Qa8+", GameState.UNFINISHED);
		moves.put("Qc2", GameState.DRAW);
		moves.put("Qe4", GameState.DRAW);

		moves.forEach((move, state) -> {
			Board simulation = endgame.copy();
			System.out.println("Testing move: " + move);
			PlayerMove playerMove = simulation.fromNotation(move);
			assertTrue(simulation.getLegalMoves().contains(playerMove));
			simulation.makeMove(playerMove);
			assertEquals(state, simulation.gameState());
			System.out.println(simulation);
			System.out.println("Position: " + simulation.toFEN());
			System.out.println("State: " + simulation.gameState());
			System.out.println("\n");
		});

		// famous opening
		Board opening = new Board();
		assertEquals(GameState.UNFINISHED, opening.gameState());
		opening.makeMove("e4");
		opening.makeMove("e5");
		opening.makeMove("Ke2");
		opening.makeMove("Ke7"); // first repetition
		opening.makeMove("Ke1");
		opening.makeMove("Ke8");
		opening.makeMove("Ke2");
		opening.makeMove("Ke7"); // second repetition
		opening.makeMove("Ke1");
		opening.makeMove("Ke8");
		opening.makeMove("Ke2");

		System.out.println(opening);
		System.out.println("Repetitions: " + opening.maxRepeatedPositions());
		System.out.println("State: " + opening.gameState());
		System.out.println("FEN: " + opening.toFEN());

		assertEquals(GameState.UNFINISHED, opening.gameState());
		opening.makeMove("Ke7"); // third repetition

		System.out.println(opening);
		System.out.println("Repetitions: " + opening.maxRepeatedPositions());
		System.out.println("State: " + opening.gameState());
		System.out.println("FEN: " + opening.toFEN());

		assertEquals(GameState.DRAW, opening.gameState());
	}

	@Test
	public void testCastle() {
		Board board = Board.fromFEN("4k3/8/8/2b5/8/8/8/4K2R w K - 0 1");

		System.out.println(board);
		System.out.println("Attempting illegal short castle: O-O");

		for (Castle castle : Castle.values()) {
			assertFalse(board.getLegalMoves().contains(castle));
		}

		assertThrowsExactly(IllegalArgumentException.class, () -> board.makeMove("O-O"));
	}
}