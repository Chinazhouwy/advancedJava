package fileDemo;

import org.junit.Test;

public class CharByte {

    @Test
    public void CharTest(){
        //char 是无符号型的，可以表示一个整数，不能表示负数 ?? 0 ~ 65535
        char a = (char) 3;
        System.out.println("=="+a);
        char b = (char) (-3);
        System.out.println("==s"+b);
    }

    @Test
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
