package com.chess.engine;

import com.chess.engine.board.Board;

public class JChess {

    public static void main(String[] args) {
        Board b = Board.createStandardBoard();
        System.out.println(b);
    }
}
