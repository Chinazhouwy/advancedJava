package com.advancedjava.io.file;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * RandomAccessFile 与字符编码示例。
 *
 * <p>本类包含几个简短测试，用于观察：
 * 1. 随机访问文件的创建方式。
 * 2. Charset 的别名信息。
 * 3. 字符串编码为字节再解码回字符串的基本过程。
 */
public class RandAccessFileTests {

    @Test
    /**
     * 演示 RandomAccessFile 以读写模式打开文件的基本写法。
     */
    public void writeFixStr() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile("aa.dat","rw");
    }

    @Test
    /**
     * 打印 UTF-8 的别名集合。
     */
    public void readFixStr(){
        System.out.println(Charset.forName("UTF-8").aliases());
    }

    @Test
    /**
     * 演示字符串编码为字节，再由字节解码回字符串。
     */
    public void charset(){
        String s = "abcdef";
        Charset cset = Charset.forName("UTF-8");
        ByteBuffer buffer = cset.encode(s);
        byte[] a = buffer.array();
        for(byte b : a){
            System.out.println(b);
        }
        System.out.println("=========");
        ByteBuffer bbuf = ByteBuffer.wrap(a);
        System.out.println(cset.decode(bbuf).toString());
    }

}
