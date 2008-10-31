package com.meidusa.amoeba.sqljep.function;

import com.meidusa.amoeba.sqljep.ASTFunNode;
import com.meidusa.amoeba.sqljep.JepRuntime;
import com.meidusa.amoeba.sqljep.ParseException;

/**
 * 针对String的hash算法
 * 
 * @author hexianmao
 * @version 2008-10-27 下午05:49:02
 */
public class StringHash extends PostfixCommand {

    private static final int _hash_len = 8;
    private static final int _bit_len  = 5;

    @Override
    public int getNumberOfParameters() {
        return 1;
    }

    @Override
    public Comparable<?>[] evaluate(ASTFunNode node, JepRuntime runtime) throws ParseException {
        node.childrenAccept(runtime.ev, null);
        Comparable<?> param = runtime.stack.pop();
        return new Comparable<?>[] { param };
    }

    /**
     * <pre>
     * 字符串hash算法：s[0]*31&circ;(n-1) + s[1]*31&circ;(n-2) + ... + s[n-1]
     * 其中s[]为字符串的字符数组，换算成程序的表达式为：
     * h = 31*h + s.charAt(i); =&gt; h = (h &lt;&lt; 5) - h + s.charAt(i);
     * 注：对hash的字符串做了长度限定，保证其结果不会超出数据类型的范围。
     * </pre>
     */
    public static Comparable<?> hash(Comparable<?> param) {
        if (param != null && param instanceof String) {
            String s = (String) param;
            long h = 0;
            for (int i = 0; (i < _hash_len && i < s.length()); i++) {
                h = (h << _bit_len) - h + s.charAt(i);
            }
            return h;
        } else {
            return null;
        }
    }

    public Comparable<?> getResult(Comparable<?>... comparables) throws ParseException {
        return hash(comparables[0]);
    }

    /**
    public static void main(String[] args) {
        String s = "~~~~~~~~~~";

        int times = 1000 * 10000;
        long st = System.nanoTime();
        for (int i = 0; i < 1; i++) {
            // StringHash.hash(s);
            System.out.println(StringHash.hash(s));
        }
        long et = System.nanoTime();

        System.out.println("take time:" + (et - st) / times + " ns/times.");
    }
    */

}
