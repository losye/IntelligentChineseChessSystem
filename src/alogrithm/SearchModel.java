package alogrithm;

import chess.Board;
import chess.Piece;
import chess.Rules;
import control.GameController;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Tong on 12.08.
 * Alpha beta search.
 */
public class SearchModel {
    private static int DEPTH = 2;
    private Board board;
    private GameController controller = new GameController();

    /**
     * 负极大值算法（Negamax）
     * 其基本原理是基于下面的公式：max (a,b) = - min ( - a, - b)
     *  输入：搜索深度
     *  输出：节点的最佳走法，及对应的最佳估值
     *
     *  初始化最优值best=负无穷大                //都是极大点
     如果depth小于等于0
        调用评估函数，并将结果赋给value
        返回value值
     否则
         生成当前所有合法的走法
         对于每一步走法
         执行走法
        value= -negaMaxSearch(depth-1)             //注意函数之前有负号
         撤销走法
        如果 value> best   best=value
         如果  depth == Max_Depth   bestMove=mv
     返回best  //返回某个搜索分支的最优评估值


     评估函数的算法:
     输入：棋局
     输出：局面对当前走方的优势

     rValue:红方的优势总和
     bValue：黑方的优势总和
     分别进行评估，具体问题具体设计，获得rValue和bValue的值

     如果当前局面是红方走棋
        return rValue-bValue;
     否则
        return bValue-rValue;
     */
    public AlphaBetaNode search(Board board) {
        this.board = board;
        if (board.pieces.size() < 28)
            DEPTH = 3;
        if (board.pieces.size() < 16)
            DEPTH = 4;
        if (board.pieces.size() < 6)
            DEPTH = 5;
        if (board.pieces.size() < 4)
            DEPTH = 6;
        long startTime = System.currentTimeMillis();
        AlphaBetaNode best = null;
        ArrayList<AlphaBetaNode> moves = generateMovesForAll(true);

        for (AlphaBetaNode n : moves) {
            /* Move*/
            Piece eaten = board.updatePiece(n.piece, n.to);
            n.value = alphaBeta(DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
            /* Select a best move during searching to save time*/
            if (best == null || n.value >= best.value)
                best = n;
            /* Back move*/
            board.updatePiece(n.piece, n.from);
            if (eaten != null) {
                board.pieces.put(eaten.key, eaten);
                board.backPiece(eaten.key);
            }
        }
        long finishTime = System.currentTimeMillis();
        System.out.println(finishTime - startTime);
        return best;
    }


    private int alphaBeta(int depth, int alpha, int beta, boolean isMax) {
        /* Return evaluation if reaching leaf node or any side won.*/
        if (depth == 0 || controller.hasWin(board) != 'x')
            return new EvalModel().eval(board, 'b');
        ArrayList<AlphaBetaNode> moves = generateMovesForAll(isMax);

        synchronized (this) {
            for (final AlphaBetaNode n : moves) {
                Piece eaten = board.updatePiece(n.piece, n.to);
            /* Is maximizing player? */
                final int finalBeta = beta;
                final int finalAlpha = alpha;
                final int finalDepth = depth;
                final int[] temp = new int[1];
                /* Only adopt multi threading strategy in depth 2. To avoid conjunction.*/
                if (depth == 2) {
                    if (isMax) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                temp[0] = Math.max(finalAlpha, alphaBeta(finalDepth - 1, finalAlpha, finalBeta, false));
                            }
                        }).run();
                        alpha = temp[0];
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                temp[0] = Math.min(finalBeta, alphaBeta(finalDepth - 1, finalAlpha, finalBeta, true));
                            }
                        }).run();
                        beta = temp[0];
                    }
                }
                else {
                    if (isMax) alpha = Math.max(alpha, alphaBeta(depth - 1, alpha, beta, false));
                    else beta = Math.min(beta, alphaBeta(depth - 1, alpha, beta, true));
                }
                board.updatePiece(n.piece, n.from);
                if (eaten != null) {
                    board.pieces.put(eaten.key, eaten);
                    board.backPiece(eaten.key);
                }
            /* Cut-off */
                if (beta <= alpha) break;
            }
        }
        return isMax ? alpha : beta;
    }

    private ArrayList<AlphaBetaNode> generateMovesForAll(boolean isMax) {
        ArrayList<AlphaBetaNode> moves = new ArrayList<AlphaBetaNode>();
        for (Map.Entry<String, Piece> stringPieceEntry : board.pieces.entrySet()) {
            Piece piece = stringPieceEntry.getValue();
            if (isMax && piece.color == 'r') continue;
            if (!isMax && piece.color == 'b') continue;
            for (int[] nxt : Rules.getNextMove(piece.key, piece.position, board))
                moves.add(new AlphaBetaNode(piece.key, piece.position, nxt));
        }
        return moves;
    }

}