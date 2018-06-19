package fileDemo;

import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    @Test
    public void Path(){
        Path absolute = Paths.get("/home");
        System.out.println(absolute);
        absolute.resolve("zhouwy");
    }


    @Test
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

    static class Orientation implements Serializable {

        public static final Orientation ONE = new Orientation(1);

        public static final Orientation TWO = new Orientation(2);

        private int value;

        private Orientation(int v){this.value = v;};

        @Override
        public String toString() {
            return this.value+"";
        }

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
        public void writeExternal(ObjectOutput out) throws IOException {
            out.write(value+1);
            System.out.println(value+"dadas");
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            value = in.read();
            System.out.println(value);
        }
    }

}
