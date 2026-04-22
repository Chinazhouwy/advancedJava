package com.advancedjava.io.file;

import org.junit.Test;

/**
 * char 与 byte 基础差异示例。
 *
 * <p>该类用最简单的打印方式说明：
 * char 是无符号 16 位编码单元，而 byte 是有符号 8 位整数，
 * 两者在取值范围和类型语义上并不相同。
 */
public class CharByte {

    @Test
    /**
     * 观察 char 在数值强转时的表现。
     */
    public void CharTest(){
        //char 是无符号型的，可以表示一个整数，不能表示负数 ?? 0 ~ 65535
        char a = (char) 3;
        System.out.println("=="+a);
        char b = (char) (-3);
        System.out.println("==s"+b);
    }

    @Test
    /**
     * 观察 byte 的有符号取值范围。
     */
    public void ByteTest(){
        //而byte是有符号型的，可以表示-128—127 的数
        byte d1 = 1;
        byte d2 = -1;
        byte d3 = 127;
        byte d4 = -128;
//        byte d5 = 128; // 如果是byte d3 = 128; 编译会报错
//        byte d6 = -129;// 如果是byte d4 = -129;编译会报错
        System.out.println(d1);
        System.out.println(d2);
        System.out.println(d3);
        System.out.println(d4);
//        System.out.println(d5);
//        System.out.println(d6);
    }

}
