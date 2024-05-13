package ax.xz.max.chess;

import ax.xz.max.chess.moves.*;
import ax.xz.max.chess.util.LimitedCache;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the state of a chess board.
 * This class is immutable.
 */
public class BoardState {

	private final BoardStateInternal board;
	private final Player currentTurn;
	private final BoardCoordinate enPassantTarget;
	private final int halfMoveClock;
	private final int fullMoveNumber;
	private final boolean whiteShortCastle;
	private final boolean whiteLongCastle;
	private final boolean blackShortCastle;
	private final boolean blackLongCastle;
	private final EnumMap<Player, Boolean> isInCheck = new EnumMap<>(Player.class);

	public BoardState(
			BoardStateInternal board,
			Player currentTurn,
			BoardCoordinate enPassantTarget,

			int halfMoveClock,
			int fullMoveNumber, // number of moves both players have made; divide by two to use
			boolean whiteShortCastle,
			boolean whiteLongCastle,
			boolean blackShortCastle,
			boolean blackLongCastle
	) {
		this.board = board;
		this.currentTurn = currentTurn;
		this.enPassantTarget = enPassantTarget;
		this.halfMoveClock = halfMoveClock;
		this.fullMoveNumber = fullMoveNumber;
		this.whiteShortCastle = whiteShortCastle;
		this.whiteLongCastle = whiteLongCastle;
		this.blackShortCastle = blackShortCastle;
		this.blackLongCastle = blackLongCastle;
	}

	public static BoardState defaultBoard() {
		return fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
	}

	public Piece pieceAt(BoardCoordinate coordinate) {
		return board.get(coordinate.rank(), coordinate.file());
	}

	public boolean isEmpty(BoardCoordinate coordinate) {
		return pieceAt(coordinate) == null;
	}

	public BoardState placePiece(Piece piece, BoardCoordinate coordinate) {
		var newBoard = board.copy();
		newBoard.set(piece, coordinate.rank(), coordinate.file());
		return new BoardState(
				newBoard,
				currentTurn,
				enPassantTarget,
				halfMoveClock,
				fullMoveNumber,
				whiteShortCastle,
				whiteLongCastle,
				blackShortCastle,
				blackLongCastle
		);
	}

	public BoardState removePiece(BoardCoordinate coordinate) {
		return placePiece(null, coordinate);
	}

	/**
	 * @param resetHalfMoves resets halfMoveClock if true.
	 *                       otherwise, increments halfMoveClock
	 */
	public BoardState prepareNextMove(boolean resetHalfMoves) {
		return new BoardState(
				board,
				currentTurn.opponent(),
				null,
				resetHalfMoves ? 0 : halfMoveClock + 1,
				fullMoveNumber + 1,
				whiteShortCastle,
				whiteLongCastle,
				blackShortCastle,
				blackLongCastle
		);
	}

	public BoardState withEnPassantTarget(BoardCoordinate target) {
		return new BoardState(
				board,
				currentTurn,
				target,
				halfMoveClock,
				fullMoveNumber,
				whiteShortCastle,
				whiteLongCastle,
				blackShortCastle,
				blackLongCastle
		);
	}

	public BoardState revokeShortCastle(Player player) {
		if (player == Player.WHITE)
			return new BoardState(
					board,
					currentTurn,
					enPassantTarget,
					halfMoveClock,
					fullMoveNumber,
					false,
					whiteLongCastle,
					blackShortCastle,
					blackLongCastle
			);
		else
			return new BoardState(
					board,
					currentTurn,
					enPassantTarget,
					halfMoveClock,
					fullMoveNumber,
					whiteShortCastle,
					whiteLongCastle,
					false,
					blackLongCastle
			);
	}

	public BoardState revokeLongCastle(Player player) {
		if (player == Player.WHITE)
			return new BoardState(
					board,
					currentTurn,
					enPassantTarget,
					halfMoveClock,
					fullMoveNumber,
					whiteShortCastle,
					false,
					blackShortCastle,
					blackLongCastle
			);
		else
			return new BoardState(
					board,
					currentTurn,
					enPassantTarget,
					halfMoveClock,
					fullMoveNumber,
					whiteShortCastle,
					whiteLongCastle,
					blackShortCastle,
					false
			);
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("  _________________\n");
		for (int i = 7; i >= 0; i--) { // flip board
			builder.append(i + 1).append("| ");
			for (Piece piece : board.getRank(i)) {
				builder.append(piece == null ? " " : piece.toChar()).append(" ");
			}
			builder.append("|\n");
		}
		builder.append("  -----------------\n")
				.append("   a b c d e f g h\n");
		return builder.toString();
	}

	public String toFEN() {
		StringBuilder builder = new StringBuilder();
		for (var row : board.ranksReversed()) {
			int spaces = 0;
			for (Piece piece : row) {
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

		builder.deleteCharAt(builder.length() - 1); // remove trailing slash
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

		switch (enPassantTarget) {
			case null -> builder.append("- ");
			case BoardCoordinate b -> builder.append(b).append(" ");
		}

		builder.append(halfMoveClock)
				.append(" ")
				.append(fullMoveNumber / 2 + 1);

		return builder.toString();
	}

	@Override
	public int hashCode() {
		String[] words = toFEN().split("\\s+"); // hash objects instead of FEN
		return Objects.hash(words[0], words[1], words[2]); // use first 3 words
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BoardState other) {
			String thisFEN = this.toFEN();
			String otherFEN = other.toFEN();

			// compare FENs, but only look at first 3 words
			String[] thisWords = Arrays.stream(thisFEN.split("\\s+")).limit(3).toArray(String[]::new);
			String[] otherWords = Arrays.stream(otherFEN.split("\\s+")).limit(3).toArray(String[]::new);
			return Arrays.deepEquals(thisWords, otherWords);
		}
		return false;
	}

	/**
	 * @return the estimated current state of the game, based on the current board state, without considering repetition
	 */
	public GameState getCurrentState() { // todo: draw by insufficient material
		if (halfMoveClock >= 100)
			return GameState.DRAW;

		if (getLegalMoves(currentTurn).isEmpty())
			return isInCheck(currentTurn) ? GameState.ofWinner(currentTurn.opponent()) : GameState.DRAW;

		return GameState.UNFINISHED;
	}

	public static BoardState fromFEN(String text) {
		String[] words = text.split("\\s+");

		String boardString = words[0];
		String[] rows = boardString.split("/");
		List<List<Piece>> board = new ArrayList<>();
		for (String rowText : List.of(rows).reversed()) {
			List<Piece> rowPieces = new ArrayList<>();
			for (char c : rowText.toCharArray()) {
				if (Character.isDigit(c)) {
					// number of spaces
					int numSpaces = c - '0';
					rowPieces.addAll(Collections.nCopies(numSpaces, null));
				} else {
					// piece
					rowPieces.add(Piece.fromChar(c));
				}
			}
			board.add(rowPieces);
		}

		Piece whiteKing = new Piece(Player.WHITE, PieceType.KING);
		Piece blackKing = new Piece(Player.BLACK, PieceType.KING);
		Piece whiteRook = new Piece(Player.WHITE, PieceType.ROOK);
		Piece blackRook = new Piece(Player.BLACK, PieceType.ROOK);

		boolean whiteKingPlaced = whiteKing.equals(board.get(Player.WHITE.homeRank()).get(4));
		boolean blackKingPlaced = blackKing.equals(board.get(Player.BLACK.homeRank()).get(4));
		boolean whiteShortRookPlaced = whiteRook.equals(board.get(Player.WHITE.homeRank()).get(7));
		boolean blackShortRookPlaced = blackRook.equals(board.get(Player.BLACK.homeRank()).get(7));
		boolean whiteLongRookPlaced = whiteRook.equals(board.get(Player.WHITE.homeRank()).get(0));
		boolean blackLongRookPlaced = blackRook.equals(board.get(Player.BLACK.homeRank()).get(0));

		boolean whiteShortCastle = whiteKingPlaced && whiteShortRookPlaced;
		boolean whiteLongCastle = whiteKingPlaced && whiteLongRookPlaced;
		boolean blackShortCastle = blackKingPlaced && blackShortRookPlaced;
		boolean blackLongCastle = blackKingPlaced && blackLongRookPlaced;

		var currentPlayer = Player.WHITE;

		if (words.length > 1)
			currentPlayer = Player.fromChar(words[1].charAt(0));

		if (words.length > 2) {
			String castlingRights = words[2];

			whiteShortCastle &= castlingRights.contains("K");
			whiteLongCastle &= castlingRights.contains("Q");
			blackShortCastle &= castlingRights.contains("k");
			blackLongCastle &= castlingRights.contains("q");
		}

		BoardCoordinate enPassantTarget = null;
		String enPassant;
		if (words.length > 3 && !(enPassant = words[3]).contains("-")) {
			enPassantTarget = BoardCoordinate.fromString(enPassant);
		}

		int halfMoves = 0;
		int numMoves = 0;

		if (words.length > 5) {
			halfMoves = Integer.parseInt(words[4]);
			int fullMoves = Integer.parseInt(words[5]);
			numMoves = switch (currentPlayer) {
				case WHITE -> fullMoves * 2 - 2;
				case BLACK -> fullMoves * 2 - 1;
			};
		}

		var realBoard = new BoardStateInternal();
		for (int rank = 0; rank < board.size(); rank++) {
			for (int file = 0; file < board.get(rank).size(); file++) {
				Piece piece = board.get(rank).get(file);
				if (piece != null) {
					realBoard.set(piece, rank, file);
				}
			}
		}

		return new BoardState(realBoard, currentPlayer, enPassantTarget, halfMoves, numMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle);
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
	private Stream<PlayerMove> attacksUsingPiece(Piece piece, BoardCoordinate position) {
		return switch (piece.type()) {
			case PAWN -> Stream.of(1, -1)
					.map(a -> position.step(piece.owner().pawnDirection(), a))
					.filter(BoardCoordinate::isValid)
					.filter(a -> !isEmpty(a) && pieceAt(a).owner() != piece.owner())
					.flatMap(a ->
							a.rank() == piece.owner().opponent().homeRank() ?
									Stream.of(Promotion.allPromotions(piece, position, a)) :
									Stream.of(new RegularMove(piece, position, a))
					)
					.map(PlayerMove.class::cast);

			case KNIGHT -> KNIGHT_STEPS.stream()
					.map(position::step)
					.filter(BoardCoordinate::isValid)
					.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
					.map(a -> (PlayerMove) new RegularMove(piece, position, a));

			case KING -> ALL_STEPS.stream()
					.map(position::step)
					.filter(BoardCoordinate::isValid)
					.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
					.map(a -> (PlayerMove) new RegularMove(piece, position, a));

			case BISHOP -> DIAGONAL_STEPS.stream()
					.flatMap(step -> {
						ArrayList<BoardCoordinate> candidates = new ArrayList<>();
						for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
							candidates.add(candidate);
							if (!isEmpty(candidate)) break;
						}
						return candidates.stream();
					})
					.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
					.map(a -> (PlayerMove) new RegularMove(piece, position, a));

			case ROOK -> ORTHOGONAL_STEPS.stream()
					.flatMap(step -> {
						ArrayList<BoardCoordinate> candidates = new ArrayList<>();
						for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
							candidates.add(candidate);
							if (!isEmpty(candidate)) break;
						}
						return candidates.stream();
					})
					.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
					.map(a -> (PlayerMove) new RegularMove(piece, position, a));

			case QUEEN -> ALL_STEPS.stream()
					.flatMap(step -> {
						ArrayList<BoardCoordinate> candidates = new ArrayList<>();
						for (BoardCoordinate candidate = position.step(step); candidate.isValid(); candidate = candidate.step(step)) {
							candidates.add(candidate);
							if (!isEmpty(candidate)) break;
						}
						return candidates.stream();
					})
					.filter(a -> isEmpty(a) || pieceAt(a).owner() != piece.owner())
					.map(a -> (PlayerMove) new RegularMove(piece, position, a));
		};
	}

	/**
	 * Gets the hypothetical moves a piece could make from another square to attack the input position,
	 * regardless of legality
	 */
	private Stream<PlayerMove> findAttacksOnCoordinate(Piece piece, BoardCoordinate position) {
		Piece enemy = new Piece(piece.owner().opponent(), piece.type());
		return attacksUsingPiece(enemy, position)
				.filter(a -> a instanceof RegularMove) // promotions can't become attacks
				.filter(a -> piece.equals(pieceAt(a.to())))
				.map(a -> new RegularMove(piece, a.to(), a.from())); // reverse move
	}

	/**
	 * Determines whether a given square is defended by the opponent,
	 * regardless of legality
	 */
	private boolean isDefendedBy(Player opponent, BoardCoordinate position) {
		// strategy: replace the position square with a piece of any type, and see if it attacks an opponent piece of the same type
		Player player = opponent.opponent();
		return Stream.of(PieceType.values())
				.flatMap(pieceType -> attacksUsingPiece(new Piece(player, pieceType), position))
				.anyMatch(move -> new Piece(opponent, move.piece().type()).equals(pieceAt(move.to())));
	}

	public boolean isInCheck(Player player) {
		return isInCheck.computeIfAbsent(player, this::isInCheck0);
	}

	private boolean isInCheck0(Player player) {
		// find king
		int kingIndex = Long.numberOfTrailingZeros(board.state()[
				switch (player) {
					case WHITE -> 5;
					case BLACK -> 11;
				}]);
		if (kingIndex == 64) throw new IllegalStateException("King not found");
		BoardCoordinate kingLocation = new BoardCoordinate(kingIndex / 8, kingIndex % 8);
		return isDefendedBy(player.opponent(), kingLocation);
	}

	private boolean canDoCastle(Castle castle) {
		for (BoardCoordinate square : castle.getClearanceSquares()) {
			if (!isEmpty(square))
				return false;
		}
		for (BoardCoordinate square : castle.getProtectedSquares()) {
			if (isDefendedBy(castle.getPlayer().opponent(), square))
				return false;
		}
		return true;
	}

	private final EnumMap<Player, Set<PlayerMove>> legalMoves = new EnumMap<>(Player.class);

	public Set<PlayerMove> getLegalMoves() {
		return getLegalMoves(currentTurn);
	}

	public Set<PlayerMove> getLegalMoves(Player currentPlayer) {
		return new HashSet<>(legalMoves.computeIfAbsent(currentPlayer, this::getLegalMoves0));
	}

	private Set<PlayerMove> getLegalMoves0(Player currentPlayer) {
		Set<PlayerMove> legalMoves = unprocessedLegalMoves(currentPlayer);
		ArrayList<PlayerMove> additionalMoves = new ArrayList<>();

		Castle shortCastle = Castle.shortCastle(currentPlayer);
		if (!isInCheck(currentPlayer) && canShortCastle(currentPlayer) && canDoCastle(shortCastle))
			additionalMoves.add(shortCastle);

		Castle longCastle = Castle.longCastle(currentPlayer);
		if (!isInCheck(currentPlayer) && canLongCastle(currentPlayer) && canDoCastle(longCastle))
			additionalMoves.add(longCastle);

		// en passant
		if (enPassantTarget != null) {
			findAttacksOnCoordinate(new Piece(currentPlayer, PieceType.PAWN), enPassantTarget)
					.map(PlayerMove::from)
					.map(from -> EnPassant.enPassant(currentPlayer, from, enPassantTarget))
					.forEach(additionalMoves::add);
		}

		additionalMoves.stream()
				.filter(a -> !a.apply(this).isInCheck(currentPlayer))
				.forEach(legalMoves::add);

		return legalMoves;
	}

	private static final int MAX_CACHE_SIZE = 500_000;
	private static final LimitedCache<BoardStateInternal, EnumMap<Player, Set<PlayerMove>>> legalMovesCache = new LimitedCache<>(MAX_CACHE_SIZE);

	private Set<PlayerMove> unprocessedLegalMoves(Player currentPlayer) {
		return new HashSet<>(
				legalMovesCache.computeIfAbsent(board, a -> new EnumMap<>(Player.class))
						.computeIfAbsent(currentPlayer, this::unprocessedLegalMoves0)
		);
	}

	private Set<PlayerMove> unprocessedLegalMoves0(Player currentPlayer) {
		ArrayList<PlayerMove> legalMoves = new ArrayList<>();

		for (PieceType pieceType : PieceType.values()) {
			var piece = new Piece(currentPlayer, pieceType);

			for (BoardCoordinate location : board.allOf(piece))
				attacksUsingPiece(piece, location).forEach(legalMoves::add);
		}

		// pawn pushes
		var pawn = new Piece(currentPlayer, PieceType.PAWN);
		for (BoardCoordinate position : board.allOf(pawn)) {
			BoardCoordinate singleStepFrom = position.step(currentPlayer.pawnDirection(), 0);
			if (isEmpty(singleStepFrom)) {
				if (singleStepFrom.rank() == currentPlayer.opponent().homeRank()) {
					legalMoves.addAll(Set.of(Promotion.allPromotions(pawn, position, singleStepFrom)));
				} else {
					legalMoves.add(new RegularMove(pawn, position, singleStepFrom));

					BoardCoordinate doubleStepFrom = singleStepFrom.step(currentPlayer.pawnDirection(), 0);
					if (position.rank() == currentPlayer.pawnRank() && isEmpty(doubleStepFrom)) {
						legalMoves.add(new RegularMove(pawn, position, doubleStepFrom));
					}
				}
			}
		}

		return legalMoves.stream()
				.filter(a -> !a.apply(this).isInCheck(currentPlayer))
				.collect(Collectors.toSet());
	}

	private record ParsedQueryParams(
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
	 *
	 * @param text The algebraic notation of the move as a string (e.g. "dxc4")
	 * @return A PlayerMove
	 */
	public PlayerMove fromNotation(String text) {
		ParsedQueryParams params = parseAlgebraicNotation(text);
		Set<PlayerMove> candidates = getLegalMoves(currentTurn);

		candidates.removeIf(candidate -> {
			MoveRecord record = new MoveRecord(this, candidate); // todo: implement
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
				!(candidate instanceof Promotion promotionMove) || promotionMove.newPiece().type() != (promotionPiece)
		));

		if (candidates.isEmpty()) throw new IllegalArgumentException("No moves found for " + text);
		if (candidates.size() > 1)
			throw new IllegalArgumentException("Move not specific enough: " + candidates.size() + " candidates found for move " + text);

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
		boolean shortCastle = !longCastle && matcher.group(2) != null;

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

	public BoardStateInternal board() {
		return board;
	}

	public Player currentTurn() {
		return currentTurn;
	}

	public int halfMoveClock() {
		return halfMoveClock;
	}

	public int fullMoveNumber() {
		return fullMoveNumber;
	}

	public int numPieces() {
		return board.numPieces();
	}
}
