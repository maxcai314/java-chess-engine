package ax.xz.max.chess;

import ax.xz.max.chess.moves.PlayerMove;

import java.util.*;
import java.util.stream.Stream;

public record BoardStateInternal(
        long[] state, EnumMap<Player,
        Set<PlayerMove>> legalMoves // todo: this is a REALLY ugly hack... this shouldn't be allowed
) {
    private static final Piece[] PIECES;
    static {
        var piecesWhite = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.WHITE, type));
        var piecesBlack = Arrays.stream(PieceType.values()).map(type -> new Piece(Player.BLACK, type));

        PIECES = Stream.concat(piecesWhite, piecesBlack).toArray(Piece[]::new);
    }

    public BoardStateInternal() {
        this(new long[PIECES.length], new EnumMap<>(Player.class));
    }

    public BoardStateInternal {
        if (state.length != PIECES.length) {
            throw new IllegalArgumentException("BoardStateInternal must have the same number of pieces as PIECES");
        }
    }

    private boolean bitAt(long l, int rank, int file) {
        return (l & (1L << (rank * 8 + file))) != 0;
    }

    public Piece get(int rank, int file) {
        for (int i = 0; i < PIECES.length; i++) {
            if (bitAt(state[i], rank, file)) {
                return PIECES[i];
            }
        }

        return null;
    }

    public void set(Piece piece, int rank, int file) {
        for (int i = 0; i < PIECES.length; i++) {
            if (PIECES[i].equals(piece)) {
                state[i] |= (1L << (rank * 8 + file));
            } else {
                state[i] &= ~(1L << (rank * 8 + file));
            }
        }
        legalMoves.clear(); // todo: this is UNHINGED AND DISGUSTING PLEASE FIX
    }

    public Piece[] getRank(int rank) {
        Piece[] result = new Piece[8];
        for (int i = 0; i < 8; i++) {
            result[i] = get(rank, i);
        }
        return result;
    }

    public Iterable<Iterable<Piece>> ranksReversed() {
        return () -> new Iterator<>() {
            private int rank = 7;

            @Override
            public boolean hasNext() {
                return rank >= 0;
            }

            @Override
            public Iterable<Piece> next() {
                return rank(rank--);
            }
        };
    }

    public Iterable<Piece> rank(int rank) {
        return () -> new Iterator<>() {
            private int file = 0;

            @Override
            public boolean hasNext() {
                return file < 8;
            }

            @Override
            public Piece next() {
                return get(rank, file++);
            }
        };
    }

    public BoardStateInternal copy() {
//        var newMap = new EnumMap<Player, Set<PlayerMove>>(Player.class);
//        for (var entry : legalMoves.entrySet()) {
//            newMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
//        }
        return new BoardStateInternal(Arrays.copyOf(state, state.length), new EnumMap<>(legalMoves));
    }
}
