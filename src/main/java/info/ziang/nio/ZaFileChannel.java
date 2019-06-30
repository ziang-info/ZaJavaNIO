package info.ziang.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ZaFileChannel {

    private static final String FILENAME = "README.md";

    public static void main(String[] args) {

        ZaFileChannel zfc = new ZaFileChannel();

        System.out.println("\n-------  viaFileInputStream -----------");

        zfc.viaFileInputStream();

        System.out.println("\n-------  viaFileChannel -----------");
        zfc.viaFileChannel();

    }

    /**
     * 是采用FileInputStream读取文件内容
     */
    public void viaFileInputStream() {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(FILENAME));
            byte[] buf = new byte[1024];
            int bytesRead = in.read(buf);
            while (bytesRead != -1) {
                for (int i = 0; i < bytesRead; i++)
                    System.out.print((char) buf[i]);
                bytesRead = in.read(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 这里通过RandomAccessFile进行操作，当然也可以通过FileInputStream.getChannel()进行操作
     */
    public void viaFileChannel() {
        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile(FILENAME, "r");
            FileChannel fileChannel = aFile.getChannel();
            ByteBuffer buf = ByteBuffer.allocate(1024);
            int bytesRead = fileChannel.read(buf);

            while (bytesRead != -1) {
                System.out.println(bytesRead);

                buf.flip();
                while (buf.hasRemaining()) {
                    System.out.print((char) buf.get());
                }
                buf.compact();
                bytesRead = fileChannel.read(buf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (aFile != null) {
                    aFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}