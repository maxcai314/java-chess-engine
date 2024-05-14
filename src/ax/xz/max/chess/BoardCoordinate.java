package ax.xz.max.chess;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record BoardCoordinate(
		int rank,
		int file
) {
	public static BoardCoordinate fromString(String s) { // example: "e4"
		return new BoardCoordinate(s.charAt(1) - '1', s.charAt(0) - 'a');
	}

	@Override
	public String toString() {
		return String.format("%c%c", file + 'a', rank + '1');
	}

	public BoardCoordinate step(int rankStep, int fileStep) {
		return new BoardCoordinate(rank + rankStep, file + fileStep);
	}

	public BoardCoordinate step(BoardCoordinate step) {
		return step(step.rank(), step.file());
	}

	public boolean isValid() {
		return rank >= 0 && rank < 8 && file >= 0 && file < 8;
	}

	public static int numPossibleSteps(BoardCoordinate start, BoardCoordinate step) {
		if (!start.isValid()) return 0;

		int verticalRemaining = Integer.MAX_VALUE;
		if (step.rank() > 0) verticalRemaining = (7 - start.rank()) / step.rank();
		else if (step.rank() < 0) verticalRemaining = start.rank() / -step.rank();

		int horizontalRemaining = Integer.MAX_VALUE;
		if (step.file() > 0) horizontalRemaining = (7 - start.file()) / step.file();
		else if (step.file() < 0) horizontalRemaining = start.file() / -step.file();

		return Math.min(verticalRemaining, horizontalRemaining);
	}
}
