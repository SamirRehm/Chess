package com.tests.chess.engine.board;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;
import com.chess.engine.board.Move;
import com.chess.engine.pieces.Piece;
import com.chess.engine.player.MoveTransition;
import com.chess.engine.player.ai.MiniMax;
import com.chess.engine.player.ai.MoveStrategy;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.chess.engine.board.Move.MoveFactory.*;
import static org.junit.Assert.*;

public class BoardTest {
    @Test
    public void initialBoard() {

        final Board board = Board.createStandardBoard();
        assertEquals(board.getCurrentPlayer().getLegalMoves().size(), 20);
        assertEquals(board.getCurrentPlayer().getOpponent().getLegalMoves().size(), 20);
        assertFalse(board.getCurrentPlayer().isInCheck());
        assertFalse(board.getCurrentPlayer().isInCheckMate());
        assertFalse(board.getCurrentPlayer().isCastled());
        // assertTrue(board.getCurrentPlayer().isKingSideCastleCapable());
        // assertTrue(board.getCurrentPlayer().isQueenSideCastleCapable());
        assertEquals(board.getCurrentPlayer(), board.getWhitePlayer());
        assertEquals(board.getCurrentPlayer().getOpponent(), board.getBlackPlayer());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheck());
        assertFalse(board.getCurrentPlayer().getOpponent().isInCheckMate());
        assertFalse(board.getCurrentPlayer().getOpponent().isCastled());
        // assertTrue(board.getCurrentPlayer().getOpponent().isKingSideCastleCapable());
        // assertTrue(board.getCurrentPlayer().getOpponent().isQueenSideCastleCapable());
        // assertTrue(board.getCurrentPlayer().toString().equals("W"));
        // assertTrue(board.getCurrentPlayer().toString().equals("Black"));

        final Iterable<Piece> allPieces =
                Iterables.concat(board.getBlackPieces(), board.getWhitePieces());
        final Iterable<Move> allMoves =
                Iterables.concat(
                        board.getWhitePlayer().getLegalMoves(),
                        board.getBlackPlayer().getLegalMoves());
        for (final Move move : allMoves) {
            assertFalse(move.isAttack());
            assertFalse(move.isCastlingMove());
            // assertEquals(MoveUtils.exchangeScore(move), 1);
        }

        assertEquals(Iterables.size(allMoves), 40);
        assertEquals(Iterables.size(allPieces), 32);
        // assertFalse(BoardUtils.isEndGame(board));
        // assertFalse(BoardUtils.isThreatenedBoardImmediate(board));
        // assertEquals(StandardBoardEvaluator.get().evaluate(board, 0), 0);
        // assertEquals(board.(35), null);
    }

    @Test
    public void initialBoard2() {

        final Board board = Board.createStandardBoard();
        // System.out.println(board.toString());
        // assertEquals(board.getCurrentPlayer().getLegalMoves().size(), 20);
        Set<String> boards = new HashSet<>();
        for (Move m : board.getCurrentPlayer().getLegalMoves()) {
            MoveTransition transition = board.getCurrentPlayer().makeMove(m);
            if (transition.getMoveStatus().isDone()) {
                Board b = transition.getBoard();
                for (Move n : b.getCurrentPlayer().getLegalMoves()) {
                    MoveTransition transition1 = b.getCurrentPlayer().makeMove(n);
                    if (transition1.getMoveStatus().isDone()) {
                        Board c = transition1.getBoard();
                        for (Move o : c.getCurrentPlayer().getLegalMoves()) {
                            MoveTransition transition2 = c.getCurrentPlayer().makeMove(o);
                            if (transition2.getMoveStatus().isDone()) {
                                Board d = transition2.getBoard();
                                for (Move p : d.getCurrentPlayer().getLegalMoves()) {
                                    MoveTransition transition3 = d.getCurrentPlayer().makeMove(p);
                                    if (transition3.getMoveStatus().isDone()) {
                                        Board e = transition3.getBoard();
                                        // boards.add(e.toString());
                                        // for(Move q : e.getCurrentPlayer().getLegalMoves()) {
                                        // MoveTransition transition4 =
                                        // e.getCurrentPlayer().makeMove(q);
                                        // if(transition4.getMoveStatus().isDone()) {
                                        // Board f = transition4.getBoard();
                                        // boards.add(f);
                                        // }
                                        // }
                                    }
                                }
                            }
                            //   Board d = c.getCurrentPlayer().makeMove(o).getBoard();
                            // for(Move p : d.getCurrentPlayer().getLegalMoves()) {
                            //    Board e = d.getCurrentPlayer().makeMove(p).getBoard();
                        }
                    }
                    // }
                    // }
                }
            }
        }
        System.out.println(boards.size());
        // for(String alpha : boards) {
        //    System.out.println(alpha);
        // }

    }

    @Test
    public void testFoolsMate() {
        final Board board = Board.createStandardBoard();
        final MoveTransition t1 =
                board.getCurrentPlayer()
                        .makeMove(
                                Move.MoveFactory.createMove(
                                        board,
                                        BoardUtils.getCoordinateAtPosition("f2"),
                                        BoardUtils.getCoordinateAtPosition("f3")));

        assertTrue(t1.getMoveStatus().isDone());
        final MoveTransition t2 =
                t1.getBoard()
                        .getCurrentPlayer()
                        .makeMove(
                                Move.MoveFactory.createMove(
                                        t1.getBoard(),
                                        BoardUtils.getCoordinateAtPosition("e7"),
                                        BoardUtils.getCoordinateAtPosition("e5")));

        assertTrue(t2.getMoveStatus().isDone());

        final MoveTransition t3 =
                t2.getBoard()
                        .getCurrentPlayer()
                        .makeMove(
                                Move.MoveFactory.createMove(
                                        t2.getBoard(),
                                        BoardUtils.getCoordinateAtPosition("g2"),
                                        BoardUtils.getCoordinateAtPosition("g4")));

        assertTrue(t3.getMoveStatus().isDone());

        final MoveStrategy strategy = new MiniMax(4);
        final Move aiMove = strategy.execute(t3.getBoard());

        final Move bestMove = Move.MoveFactory.createMove(
                t3.getBoard(),
                BoardUtils.getCoordinateAtPosition("d8"),
                BoardUtils.getCoordinateAtPosition("h4"));

        assertEquals(aiMove, bestMove);
    }
}
