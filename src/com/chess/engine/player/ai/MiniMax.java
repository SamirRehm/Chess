package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MiniMax implements MoveStrategy {

    private final BoardEvaluator boardEvaluator;
    private final int depth;

    public MiniMax(final int depth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "Minimax";
    }

    @Override
    public Move execute(Board board) {

        final long startTime = System.currentTimeMillis();
        Move bestMove = null;
        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.getCurrentPlayer() + "THINKING with depth = " + depth);
        ExecutorService executorService = Executors.newFixedThreadPool(8);

        List<Callable<Integer>> runners = new ArrayList<>();
        List<Move> all_valid_moves = new ArrayList<>();
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                all_valid_moves.add(move);
                MinMaxRunner minMaxRunner = new MinMaxRunner(moveTransition.getBoard());
                runners.add(minMaxRunner);
            }
        }
        List<Future<Integer>> results = new ArrayList<>();
        try {
            results = executorService.invokeAll(runners);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Integer> integer_results = new ArrayList<>();
        for (Future<Integer> result : results) {
            try {
                integer_results.add(result.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        int index = -1;
        if (board.getCurrentPlayer().getAlliance().isBlack()) {
            index =
                    IntStream.range(0, integer_results.size())
                            .reduce(
                                    (i, j) ->
                                            integer_results.get(i) < integer_results.get(j) ? i : j)
                            .getAsInt();
        } else {
            index =
                    IntStream.range(0, integer_results.size())
                            .reduce(
                                    (i, j) ->
                                            integer_results.get(i) > integer_results.get(j) ? i : j)
                            .getAsInt();
        }
        System.out.println("Done thinking, took time " + (int)(System.currentTimeMillis() - startTime));
        return all_valid_moves.get(index);
    }

    public class MinMaxRunner implements Callable<Integer> {

        private final Board board;
        public MinMaxRunner(final Board board) {
            this.board = board;
        }

        @Override
        public Integer call() throws Exception {
            return board.getCurrentPlayer().getOpponent().getAlliance().isWhite() ?
                    min(board, depth - 1) :
                    max(board, depth - 1);
        }
    }


    public int min(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getBoard(), depth-1);
                if(currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
            }
        }
        return lowestSeenValue;
    }

    private static boolean isEndGameScenario(final Board board) {
        return board.getCurrentPlayer().isInCheckMate() || board.getCurrentPlayer().isInStaleMate();
    }

    public int max(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getBoard(), depth-1);
                if(currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }
        return highestSeenValue;
    }
}
