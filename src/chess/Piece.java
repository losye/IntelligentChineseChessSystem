package chess;

/**
 * Created by Tong on 12.03.
 * Chess > Piece entity
 */
public class Piece implements Cloneable {
    public String key;
    public char color;
    public char character;
    public char index;
    public int[] position = new int[2];

    /**
     * name 例如为 "bj1"
     * color 就是 black , character 对应j 表示车 , index 对应1 表示右边的车 ,postion对应[0,8] 为初始化位置
     * @param name
     * @param position
     */
    public Piece(String name, int[] position) {
        this.key = name;
        this.color = name.charAt(0);
        this.character = name.charAt(1);
        this.index = name.charAt(2);
        this.position = position;
    }

}
