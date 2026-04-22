package com.advancedjava.io.file;

import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件与对象序列化小实验集合。
 *
 * <p>本类的重点不在“工具方法封装”，而在于通过若干测试方法观察：
 * 1. Path/Paths 的路径拼接行为。
 * 2. {@code readResolve()} 如何把反序列化对象替换为单例常量。
 * 3. {@code Externalizable} 如何自定义序列化协议。
 */
public class FileUtils {

    @Test
    /**
     * 演示 Path 的创建与路径拼接行为。
     */
    public void Path(){
        Path absolute = Paths.get("/home");
        System.out.println(absolute);
        absolute.resolve("zhouwy");
    }


    @Test
    /**
     * 演示 readResolve 如何把反序列化对象替换为预定义单例。
     */
    public void readResolveTest() throws IOException, ClassNotFoundException {
        Orientation or = Orientation.ONE;
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.dat"));
        out.writeObject(or);
        out.close();
        ObjectInputStream input = new ObjectInputStream(new FileInputStream("data.dat"));
        Orientation sa = (Orientation)input.readObject();
        System.out.println(sa);
        System.out.println(sa.equals(Orientation.ONE));
    }

    @Test
    /**
     * 演示 Externalizable 通过自定义协议控制读写过程。
     */
    public void externalTest() throws IOException, ClassNotFoundException {

        Orient or = Orient.ONE;
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.dat"));
        out.writeObject(or);
        out.close();
        ObjectInputStream input = new ObjectInputStream(new FileInputStream("data.dat"));
        Orient sa = (Orient)input.readObject();
        System.out.println(sa+" cccc");
        System.out.println(sa.equals(Orient.ONE));

    }

    /**
     * 模拟单例对象在反序列化阶段被折叠回固定实例。
     */
    static class Orientation implements Serializable {

        public static final Orientation ONE = new Orientation(1);

        public static final Orientation TWO = new Orientation(2);

        private int value;

        private Orientation(int v){this.value = v;};

        @Override
        public String toString() {
            return this.value+"";
        }

        /**
         * 根据 value 返回预定义常量，避免产生新的等价对象。
         */
        protected Object readResolve(){
            if(this.value == 1 ){
                System.out.println(value+" aaaa");
                return Orientation.ONE;
            }

            if(this.value == 2 ){
                System.out.println(value);
                return Orientation.TWO;
            }
            return  null;
        }
    }

    /**
     * 演示 Externalizable 与 readResolve 组合时的行为。
     */
    static class Orient implements Externalizable {

        public static final Orient ONE = new Orient(1);

        public static final Orient TWO = new Orient(2);

        private int value;

        public Orient(){};

        private Orient(int v){this.value = v;};

        @Override
        public String toString() {
            return this.value+"";
        }

        /**
         * 序列化后将对象替换回预定义常量。
         */
        protected Object readResolve(){
            if(this.value == 1 ){
                System.out.println(value+" aaaa");
               return Orient.ONE;
            }

            if(this.value == 2 ){
                System.out.println(value+" bbbb");
                return Orient.TWO;
            }
            return  null;
        }

        @Override
        /**
         * 自定义写出格式，这里故意写入 value + 1 以便观察效果。
         */
        public void writeExternal(ObjectOutput out) throws IOException {
            out.write(value+1);
            System.out.println(value+"dadas");
        }

        @Override
        /**
         * 按自定义协议读取数据。
         */
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = in.read();
            System.out.println(value);
        }
    }

}
