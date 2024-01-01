package ax.xz.max.chess;

import ax.xz.max.chess.moves.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Represents the state of a chess board.
 * This class is immutable.
 */
public record BoardState (
	List<List<Piece>> board,
	Player currentTurn,
	BoardCoordinate enPassantTarget,
	int halfMoveClock,
	int fullMoveNumber, // number of moves both players have made; divide by two to use
	boolean whiteShortCastle,
	boolean whiteLongCastle,
	boolean blackShortCastle,
	boolean blackLongCastle
) {
	public BoardState {
		board = board.stream().map(Collections::unmodifiableList).toList();
		if (board.size() != 8)
			throw new IllegalArgumentException("Board must be 8x8");
		for (List<Piece> row : board)
			if (row.size() != 8)
				throw new IllegalArgumentException("Board must be 8x8");
	}

	public static BoardState defaultBoard() {
		return fromFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
	}

	public Piece pieceAt(BoardCoordinate coordinate) {
		return board.get(coordinate.rank()).get(coordinate.file());
	}

	public boolean isEmpty(BoardCoordinate coordinate) {
		return pieceAt(coordinate) == null;
	}

	public BoardState placePiece(Piece piece, BoardCoordinate coordinate) {
		var newBoard = board.stream().<List<Piece>>map(ArrayList::new).toList();
		newBoard.get(coordinate.rank()).set(coordinate.file(), piece);
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
	 * @param resetHalfMoves
	 * resets halfMoveClock if true.
	 * otherwise, increments halfMoveClock
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
		for (int i=7;i>=0;i--) { // flip board
			builder.append(i + 1).append("| ");
			for (Piece piece : board.get(i)) {
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
		for (List<Piece> row : board.reversed()) {
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

		Optional<PlayerMove> passants = getLegalMoves().stream()
				.filter(a -> a instanceof EnPassant)
				.findAny();
		passants.ifPresentOrElse(playerMove -> builder.append(playerMove.to()).append(" "), () -> builder.append("- "));

		builder.append(halfMoveClock)
				.append(" ")
				.append(fullMoveNumber / 2 + 1);

		return builder.toString();
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(
				Arrays.stream(toFEN().split("\\s+")).limit(3).toArray(String[]::new)
		);
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

		return new BoardState(board, currentPlayer, enPassantTarget, halfMoves, numMoves, whiteShortCastle, whiteLongCastle, blackShortCastle, blackLongCastle);
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
		var kingLocation = IntStream.range(0, 8)
				.mapToObj(BoardCoordinate::allFromRank)
				.flatMap(Set::stream)
				.filter(a -> king.equals(pieceAt(a)))
				.findAny()
				.orElseThrow();
		return isDefendedBy(player.opponent(), kingLocation);
	}

	public Set<PlayerMove> getLegalMoves() {
		return getLegalMoves(currentTurn);
	}

	public Set<PlayerMove> getLegalMoves(Player currentPlayer) {
		ArrayList<PlayerMove> legalMoves = new ArrayList<>();

		for (int rank = 0; rank < board.size(); rank++) {
			for (int file = 0; file < board.get(rank).size(); file++) {
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
					if (!isEmpty(square))
						break ShortCastleSearch;
					if (isDefendedBy(currentPlayer.opponent(), square))
						break ShortCastleSearch;
				}
				legalMoves.add(castle);
			}
		}

		if (!isInCheck(currentPlayer) && canLongCastle(currentPlayer)) {
			LongCastleSearch: {
				// check if the squares are defended
				Castle castle = Castle.longCastle(currentPlayer);
				for (BoardCoordinate square : castle.getClearanceSquares()) {
					if (!isEmpty(square))
						break LongCastleSearch;
					if (isDefendedBy(currentPlayer.opponent(), square))
						break LongCastleSearch;
				}
				legalMoves.add(castle);
			}
		}

		// en passant
		if (enPassantTarget != null) {
			findAttacksOnCoordinate(new Piece(currentPlayer, PieceType.PAWN), enPassantTarget)
					.stream()
					.map(PlayerMove::from)
					.map(from -> EnPassant.enPassant(currentPlayer, from, enPassantTarget))
					.forEach(legalMoves::add);
		}

		return legalMoves.stream()
				.filter(a -> !a.apply(this).isInCheck(currentPlayer))
				.collect(Collectors.toSet());
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
}
