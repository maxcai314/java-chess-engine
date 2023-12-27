package ax.xz.max.chess;

import ax.xz.max.chess.moves.*;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
	private final ArrayList<MoveRecord> moves;

	// castling rights (revoked when king or rook game.moves)
	public boolean whiteShortCastle;
	public boolean whiteLongCastle;
	public boolean blackShortCastle;
	public boolean blackLongCastle;

	private int halfMoves; // for 50-move rule

	private int numMoves; // number of moves both players have made; divide by two to use

	private Board(Piece[][] board, Player currentTurn, ArrayList<MoveRecord> moves, boolean whiteShortCastle, boolean whiteLongCastle, boolean blackShortCastle, boolean blackLongCastle, int halfMoves, int numMoves) {
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
			return new Board(board, Player.WHITE, new ArrayList<>(), true, true, true, true, 0, 0);
		}

		Player currentPlayer = Player.fromChar(words[1].charAt(0));

		if (words.length == 2) {
			return new Board(board, currentPlayer, new ArrayList<>(), true, true, true, true, 0, 0);
		}

		String castlingRights = words[2];
		boolean whiteShortCastle = castlingRights.contains("K");
		boolean whiteLongCastle = castlingRights.contains("Q");
		boolean blackShortCastle = castlingRights.contains("k");
		boolean blackLongCastle = castlingRights.contains("q");

		if (words.length == 3) {
			return new Board(board, currentPlayer, new ArrayList<>(), whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, 0, 0);
		}

		String enPassant = words[3];
		ArrayList<MoveRecord> prevMoves = new ArrayList<>();

		Board result;

		if (words.length < 5) {
			result = new Board(board, currentPlayer, prevMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, 0, 0);
		} else {
			int halfMoves = Integer.parseInt(words[4]);
			int fullMoves = Integer.parseInt(words[5]);
			int numMoves = switch (currentPlayer) {
				case WHITE -> fullMoves * 2 - 2;
				case BLACK -> fullMoves * 2 - 1;
			};
			result = new Board(board, currentPlayer, prevMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, halfMoves, numMoves);
		}

		if (!enPassant.equals("-")) {
			BoardCoordinate possibleCapture = BoardCoordinate.fromString(enPassant);
			// figure out the previous opponent move
			Player opponent = currentPlayer.opponent();
			Piece opponentPawn = new Piece(opponent, PieceType.PAWN);
			BoardCoordinate opponentPawnFrom = new BoardCoordinate(opponent.pawnRank(), possibleCapture.file());
			BoardCoordinate opponentPawnTo = opponentPawnFrom.step(2 * opponent.pawnDirection(), 0);
			BoardCoordinate captureTile = opponentPawnFrom.step(opponent.pawnDirection(), 0);

			if (captureTile.equals(possibleCapture)) {
				// figure out the previous board
				board[opponentPawnFrom.rank()][opponentPawnFrom.file()] = opponentPawn;
				board[opponentPawnTo.rank()][opponentPawnTo.file()] = null;
				result = new Board(board, opponent, prevMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle, 0, result.numMoves - 1);
				result.makeMove(new RegularMove(opponentPawn, opponentPawnFrom, opponentPawnTo));
			}
		}
		return result;
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

	private record ParsedQueryParams (
			boolean shortCastle,
			boolean longCastle,
			PieceType piece,
			OptionalInt rankContext,
			OptionalInt fileContext,
			boolean isCapture,
			BoardCoordinate destination,
			Optional<PieceType> promotion,
			boolean isCheck,
			boolean isMate
	) {
		public ParsedQueryParams {
			// throw if logic error
			if (shortCastle && longCastle)
				throw new IllegalStateException("Can not be both short and long castle");
			if (isMate && !isCheck)
				throw new IllegalStateException("All checkmates must be a check as well");
		}
	}

	/**
	 * Converts algebraic notation into a PlayerMove
	 * @param text The algebraic notation of the move as a string (e.g. "dxc4")
	 * @return A PlayerMove
	 */
	public PlayerMove fromNotation(String text) {
		ParsedQueryParams params = parseAlgebraicNotation(text);
		Set<PlayerMove> candidates = getLegalMoves(currentTurn);

		candidates.removeIf(candidate -> {
			MoveRecord record = copy().makeMove(candidate);
			return record.isCheck() != (params.isCheck())
					|| record.isCapture() != params.isCapture()
					|| record.isMate() != params.isMate();
		});

		if (params.shortCastle()) {
			Castle castle = Castle.shortCastle(currentTurn);
			if (candidates.contains(castle)) return castle;
			else throw new IllegalArgumentException("Castling " + text + " not allowed");
		}
		if (params.longCastle()) {
			Castle castle = Castle.longCastle(currentTurn);
			if (candidates.contains(castle)) return castle;
			else throw new IllegalArgumentException("Castling " + text + " not allowed");
		}

		candidates.removeIf(candidate -> candidate.piece().type() != params.piece());

		candidates.removeIf(candidate -> !candidate.to().equals(params.destination()));
		params.rankContext().ifPresent(rank -> candidates.removeIf(candidate -> candidate.from().rank() != rank));
		params.fileContext().ifPresent(file -> candidates.removeIf(candidate -> candidate.from().file() != file));

		params.promotion().ifPresent(promotionPiece -> candidates.removeIf(candidate ->
				!(candidate instanceof Promotion promotionMove) || promotionMove.newPiece().type() !=(promotionPiece)
		));

		if (candidates.isEmpty()) throw new IllegalArgumentException("No moves found for " + text);
		if (candidates.size() > 1) throw new IllegalArgumentException("Move not specific enough: " + candidates.size() + " candidates found for move " + text);

		return candidates.iterator().next();
	}

	private static final String ALGEBRAIC_REGEX_PATTERN = "(([Oo0]-[Oo0](-[Oo0])?)|([KQRBN])?([a-h])??([1-8])??(x)?([a-h][1-8])(=[QRBN])?)([+#])?";

	/**
	 * Parses algebraic notation into a {@link ParsedQueryParams} object.
	 */
	private static ParsedQueryParams parseAlgebraicNotation(String text) {
		Pattern pattern = Pattern.compile(ALGEBRAIC_REGEX_PATTERN);
		Matcher matcher = pattern.matcher(text);
		if (!matcher.matches()) throw new IllegalArgumentException("Invalid algebraic notation: " + text);

		boolean longCastle = matcher.group(3) != null;
		boolean shortCastle =  !longCastle && matcher.group(2) != null;

		PieceType piece = switch (matcher.group(4)) {
			case String s -> PieceType.fromChar(s.charAt(0));
			case null -> (longCastle || shortCastle) ? null : PieceType.PAWN;
		};

		OptionalInt rankContext = switch (matcher.group(6)) {
			case String s -> OptionalInt.of(s.charAt(0) - '1');
			case null -> OptionalInt.empty();
		};

		OptionalInt fileContext = switch (matcher.group(5)) {
			case String s -> OptionalInt.of(s.charAt(0) - 'a');
			case null -> OptionalInt.empty();
		};

		boolean capture = matcher.group(7) != null;

		BoardCoordinate destination = Optional.ofNullable(matcher.group(8)).map(BoardCoordinate::fromString).orElse(null);

		Optional<PieceType> promotion = Optional.ofNullable(matcher.group(9))
				.map(a -> a.charAt(1))
				.map(PieceType::fromChar);

		boolean isMate = switch (matcher.group(10)) {
			case String s -> s.startsWith("#");
			case null -> false;
		};

		boolean isCheck = isMate || switch (matcher.group(10)) {
			case String s -> s.startsWith("+");
			case null -> false;
		};

		return new ParsedQueryParams(
				shortCastle,
				longCastle,
				piece,
				rankContext,
				fileContext,
				capture,
				destination,
				promotion,
				isCheck,
				isMate
		);
	}

	public boolean isEmpty(BoardCoordinate coordinate) {
		return pieceAt(coordinate) == null;
	}

	private static final Set<BoardCoordinate> DIAGONAL_STEPS = Set.of(
			new BoardCoordinate(1, 1),
			new BoardCoordinate(1, -1),
			new BoardCoordinate(-1, 1),
			new BoardCoordinate(-1, -1)
	);

	private static final Set<BoardCoordinate> ORTHOGONAL_STEPS = Set.of(
			new BoardCoordinate(1, 0),
			new BoardCoordinate(0, 1),
			new BoardCoordinate(-1, 0),
			new BoardCoordinate(0, -1)
	);

	private static final Set<BoardCoordinate> ALL_STEPS = Set.of(
			new BoardCoordinate(1, 1),
			new BoardCoordinate(1, 0),
			new BoardCoordinate(1, -1),
			new BoardCoordinate(0, 1),
			new BoardCoordinate(0, -1),
			new BoardCoordinate(-1, 1),
			new BoardCoordinate(-1, 0),
			new BoardCoordinate(-1, -1)
	);

	private static final Set<BoardCoordinate> KNIGHT_STEPS = Set.of(
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
	 * Gets the hypothetical moves a piece could make if it were of the input type and at the input position,
	 * regardless of legality
	 */
	private Set<PlayerMove> attacksUsingPiece(Piece piece, BoardCoordinate position) {
		return switch (piece.type()) {
			case PAWN ->
					Stream.of(1, -1)
						.map(a -> position.step(piece.owner().pawnDirection(), a))
						.filter(BoardCoordinate::isValid)
						.filter(a -> !isEmpty(a) && pieceAt(a).owner() != piece.owner())
						.flatMap(a ->
						a.rank() == piece.owner().opponent().homeRank() ?
							Stream.of(Promotion.allPromotions(piece, position, a)) :
							Stream.of(new RegularMove(piece, position, a))
						)
						.map(PlayerMove.class::cast)
						.collect(Collectors.toSet());

			case KNIGHT ->
					KNIGHT_STEPS.stream()
						.map(position::step)
						.filter(BoardCoordinate::isValid)
						.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
						.map(a -> (PlayerMove) new RegularMove(piece, position, a))
						.collect(Collectors.toSet());

			case KING ->
					ALL_STEPS.stream()
						.map(position::step)
						.filter(BoardCoordinate::isValid)
						.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
						.map(a -> (PlayerMove) new RegularMove(piece, position, a))
						.collect(Collectors.toSet());

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
						.collect(Collectors.toSet());

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
						.collect(Collectors.toSet());

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
						.collect(Collectors.toSet());
		};
	}

	/**
	 * Gets the hypothetical moves a piece could make from another square to attack the input position,
	 * regardless of legality
	 */
	private Set<PlayerMove> findAttacksOnCoordinate(Piece piece, BoardCoordinate position) {
		Piece enemy = new Piece(piece.owner().opponent(), piece.type());
		return attacksUsingPiece(enemy, position)
				.stream()
				.filter(a -> a instanceof RegularMove) // promotions can't become attacks
				.filter(a -> piece.equals(pieceAt(a.to())))
				.map(a -> (PlayerMove) new RegularMove(piece, a.to(), a.from())) // reverse move
				.collect(Collectors.toSet());
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
	 * Determines whether a given square is defended by the opponent,
	 * regardless of legality
	 */
	private boolean isDefendedBy(Player opponent, BoardCoordinate position) {
		// strategy: replace the position square with a piece of any type, and see if it attacks an opponent piece of the same type
		Player player = opponent.opponent();
		return Stream.of(ATTACKING_PIECES)
				.flatMap(pieceType -> attacksUsingPiece(new Piece(player, pieceType), position).stream())
				.anyMatch(move -> pieceAt(move.to()) != null && move.piece().type() == pieceAt(move.to()).type());
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

	public Set<PlayerMove> getLegalMoves() {
		return getLegalMoves(currentTurn);
	}

	public Set<PlayerMove> getLegalMoves(Player currentPlayer) {
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
							legalMoves.addAll(Set.of(Promotion.allPromotions(piece, position, singleStepFrom)));
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
				Castle castle = Castle.shortCastle(currentPlayer);
				for (BoardCoordinate square : castle.getClearanceSquares()) {
					if (isDefendedBy(currentPlayer.opponent(), square))
						break ShortCastleSearch;
				}
				legalMoves.add(Castle.shortCastle(currentPlayer));
			}
		}

		if (!isInCheck(currentPlayer) && canLongCastle(currentPlayer)) {
			LongCastleSearch: {
				// check if the squares are defended
				Castle castle = Castle.longCastle(currentPlayer);
				for (BoardCoordinate square : castle.getClearanceSquares()) {
					if (isDefendedBy(currentPlayer.opponent(), square))
						break LongCastleSearch;
				}
				legalMoves.add(Castle.longCastle(currentPlayer));
			}
		}

		// en passant
		if (!moves.isEmpty() && moves.getLast().move() instanceof RegularMove lastMove) {
			if (lastMove.piece().type() == PieceType.PAWN) {
				BoardCoordinate pawnFrom = new BoardCoordinate(currentPlayer.opponent().pawnRank(), lastMove.from().file());
				BoardCoordinate capturablePawn = pawnFrom.step(currentPlayer.opponent().pawnDirection(), 0);
				BoardCoordinate pawnTo = pawnFrom.step(2 * currentPlayer.opponent().pawnDirection(), 0);
				if (lastMove.from().equals(pawnFrom) &&
						lastMove.to().equals(pawnTo) &&
						isEmpty(capturablePawn) && isEmpty(pawnFrom) &&
						!isEmpty(pawnTo) &&
						pieceAt(pawnTo).equals(new Piece(currentPlayer.opponent(), PieceType.PAWN))
				) {
					findAttacksOnCoordinate(new Piece(currentPlayer, PieceType.PAWN), capturablePawn)
						.stream()
						.map(PlayerMove::from)
						.map(from -> EnPassant.enPassant(currentPlayer, from, capturablePawn))
						.forEach(legalMoves::add);
				}
			}
		}

		return legalMoves.stream()
				.filter(a -> a.isPossible(this))
				.filter(a -> {
					Board copy = copy();
					copy.makeMove(a); // execute move without creating move metadata, infinite loop
					return !copy.isInCheck(currentPlayer);
				})
				.collect(Collectors.toSet());
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

	public MoveRecord makeMove(PlayerMove move) {
		Board prevBoard = copy();
		move.execute(this);
		MoveRecord record = new MoveRecord(prevBoard, move);
		moves.add(record);
		return record;
	}

	public MoveRecord makeMove(String notation) {
		return makeMove(fromNotation(notation));
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

	public GameState getState() {
		if (maxRepeatedPositions() >= 3)
			return GameState.DRAW;
		if (halfMoves >= 100)
			return GameState.DRAW;

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

		Optional<PlayerMove> passants = getLegalMoves().stream()
				.filter(a -> a instanceof EnPassant)
				.findAny();
		passants.ifPresentOrElse(playerMove -> builder.append(playerMove.to()).append(" "), () -> builder.append("- "));

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
			return Arrays.deepEquals(thisWords, otherWords);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(
				Arrays.stream(toFEN().split("\\s+")).limit(3).toArray(String[]::new)
		);
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
