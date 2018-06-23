package src.main.java.com.zhouwy.advancedjava.io.file;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class RandAccessFileTests {

    @Test
    public void writeFixStr() throws FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile("aa.dat","rw");
    }

    @Test
    public void readFixStr(){
        System.out.println(Charset.forName("UTF-8").aliases());
    }

    @Test
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
