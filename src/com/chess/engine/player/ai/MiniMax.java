package com.chess.engine.player.ai;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.player.MoveTransition;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class MiniMax implements MoveStrategy {

    private final MoveSorter moveSorter;

    private enum MoveSorter {

        SORT {
            @Override
            Collection<Move> sort(final Collection<Move> moves) {
                return Ordering.from(SMART_SORT).immutableSortedCopy(moves);
            }
        };

        public static Comparator<Move> SMART_SORT = new Comparator<Move>() {
            @Override
            public int compare(final Move move1, final Move move2) {
                return ComparisonChain.start()
                        .compareTrueFirst(move1.isAttack(), move2.isAttack())
                        .compareTrueFirst(move1.isCastlingMove(), move2.isCastlingMove())
                        .compare(move2.getMovedPiece().getPieceValue(), move1.getMovedPiece().getPieceValue())
                        .result();
            }
        };

        abstract Collection<Move> sort(Collection<Move> moves);
    }


    private final BoardEvaluator boardEvaluator;
    private final int depth;

    public MiniMax(final int depth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.depth = depth;
        this.moveSorter = MoveSorter.SORT;
    }

    @Override
    public String toString() {
        return "Minimax";
    }

    @Override
    public Move execute(Board board) {
        //Move best_move_guaranteed = execute_2(board);

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
        /*if (best_move_guaranteed != all_valid_moves.get(index)) {
            System.out.println("THIS IS REALLY BAD");
            System.out.println(best_move_guaranteed);
            System.out.println(all_valid_moves.get(index));
        }*/
        return all_valid_moves.get(index);
    }


    public Move execute_2(Board board) {

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
                MinMaxRunner_orig minMaxRunner = new MinMaxRunner_orig(moveTransition.getBoard());
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
            return board.getCurrentPlayer().getOpponent().getAlliance().isWhite()
                    ? min(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE)
                    : max(board, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }

    public class MinMaxRunner_orig implements Callable<Integer> {

        private final Board board;
        public MinMaxRunner_orig(final Board board) {
            this.board = board;
        }

        @Override
        public Integer call() throws Exception {
            return board.getCurrentPlayer().getOpponent().getAlliance().isWhite()
                    ? min_orig(board, depth - 1)
                    : max_orig(board, depth - 1);
        }
    }


    public int min(final Board board, final int depth, int highest, int lowest) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowestSeenValue = lowest;
        for(final Move move : moveSorter.sort(board.getCurrentPlayer().getLegalMoves())) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max(moveTransition.getBoard(), depth-1, highest, lowestSeenValue);
                if(currentValue <= lowestSeenValue) {
                    lowestSeenValue = currentValue;
                }
                if(lowestSeenValue <= highest) {
                    break;
                }
            }
        }
        return lowestSeenValue;
    }

    public int min_orig(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int lowestSeenValue = Integer.MAX_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = max_orig(moveTransition.getBoard(), depth-1);
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

    public int max(final Board board, final int depth, int highest, int lowest) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenValue = highest;
        for(final Move move : moveSorter.sort( board.getCurrentPlayer().getLegalMoves())) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min(moveTransition.getBoard(), depth-1, highestSeenValue, lowest);
                if(currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
                if(lowest <= highestSeenValue) {
                    break;
                }
            }
        }
        return highestSeenValue;
    }

    public int max_orig(final Board board, final int depth) {
        if(depth == 0 || isEndGameScenario(board)) {
            return this.boardEvaluator.evaluate(board, depth);
        }
        int highestSeenValue = Integer.MIN_VALUE;
        for(final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.getCurrentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                final int currentValue = min_orig(moveTransition.getBoard(), depth-1);
                if(currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                }
            }
        }
        return highestSeenValue;
    }
}
