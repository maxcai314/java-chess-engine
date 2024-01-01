package ax.xz.max.chess;

import ax.xz.max.chess.moves.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Board {

	private final AtomicReference<BoardState> state;
	private final ArrayList<MoveRecord> moves;

	private Board(AtomicReference<BoardState> state, ArrayList<MoveRecord> moves) {
		this.state = state;
		this.moves = moves;
	}

	public Board(BoardState state, ArrayList<MoveRecord> moves) {
		this(new AtomicReference<>(state), moves);
	}

	public Board(BoardState state) {
		this(state, new ArrayList<>());
	}

	public Board() {
		this(BoardState.defaultBoard(), new ArrayList<>());
	}

	public static Board fromFEN(String FEN) {
		return new Board(BoardState.fromFEN(FEN));
	}

	public String toFEN() {
		return boardState().toFEN();
	}

	@Override
	public String toString() {
		return boardState().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Board that)) return false;
		return this.boardState().equals(that.boardState());
	}

	public BoardState boardState() {
		return state.get();
	}

	/** @return the number of individual moves made on the Board object */
	public int getNumMoves() {
		return moves.size(); // number of moves made since construction of object
	}

	public Player currentTurn() {
		return boardState().currentTurn();
	}

	public Piece pieceAt(BoardCoordinate coordinate) {
		return boardState().pieceAt(coordinate);
	}

	public Board copy() {
		return new Board(boardState(), new ArrayList<>(moves));
	}

	public String analysisLink() {
		String FEN = boardState().toFEN();
		// replace spaces in FEN with underscores
		FEN = FEN.replaceAll("\\s+", "_");
		return "https://lichess.org/analysis/" + FEN;
	}

	public PlayerMove fromNotation(String notation) {
		return boardState().fromNotation(notation);
	}

	public Set<PlayerMove> getLegalMoves() {
		return boardState().getLegalMoves();
	}

	public Set<PlayerMove> getLegalMoves(Player player) {
		return boardState().getLegalMoves(player);
	}

	public MoveRecord makeMove(String notation) {
		return makeMove(fromNotation(notation));
	}

	public MoveRecord makeMove(PlayerMove move) {
		BoardState prevBoard = boardState();
		state.getAndUpdate(move);
		var record = new MoveRecord(prevBoard, move);
		moves.add(record);
		return record;
	}

	private int indexOf(MoveRecord moveRecord) {
		for (int i = moves.size()-1; i >= 0; i--) {
			if (moves.get(i) == moveRecord)
				return i;
		}
		return -1; // not found
	}

	public void unmakeMove(MoveRecord moveRecord) {
		int index = indexOf(moveRecord);
		if (index == -1)
			throw new IllegalArgumentException("MoveRecord not found in Board object");

		state.set(moveRecord.prevBoard());
		moves.subList(index, moves.size()).clear();
	}

	public boolean isInCheck(Player player) {
		return boardState().isInCheck(player);
	}

	public int maxRepeatedPositions() {
		// count duplicates using Stream
		return moves.stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.values()
				.stream()
				.mapToInt(Long::intValue)
				.max()
				.orElse(0);
	}

	public GameState gameState() {
		if (maxRepeatedPositions() >= 3)
			return GameState.DRAW;

		var board = boardState();
		if (board.getCurrentState() != GameState.UNFINISHED)
			return board.getCurrentState();

		// todo: draw by insufficient material

		return GameState.UNFINISHED;
	}
}
