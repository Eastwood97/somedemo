
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 为什么NIO的性能比IO好?
 * 1.io是面向流的，也就是读取数据的时候是从流上逐个读取，所以数据不能进行整体以为，没有缓冲区;nio是面向缓冲区的，数据是存储在缓冲区中，读取数据是在缓冲区中进行，所以进行数据的偏移操作更加方便
 * 2，io是阻塞的，当一个线程操作io时如果当前没有数据可读，那么线程阻塞，nio由于是对通道操作io，所以是非阻塞，当一个通道无数据可读，可切换通道处理其他io
 * 3，nio有selecter选择器，就是线程通过选择器可以选择多个通道，而io只能处理一个
 */
public class SimpleFileTransferTest {
    private long transferFile(File source , File des) throws IOException {
        long startTime=System.currentTimeMillis();
        if(!des.exists())
            des.createNewFile();
            BufferedInputStream bis=new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(des));
            //将数据源读到的内容写入目的地，使用数组
            byte[] bytes=new byte[1024*1024];
            int len;
            while((len=bis.read(bytes))!=-1){
                bos.write(bytes,0,len);
            }
            long endTime=System.currentTimeMillis();
            return  endTime-startTime;
    }
    private long transferFileWithNIO(File source, File des) throws IOException {
        long startTime = System.currentTimeMillis();
        if (!des.exists())
            des.createNewFile();
        RandomAccessFile read =new RandomAccessFile(source,"rw");
        RandomAccessFile write=new RandomAccessFile(des,"rw");
        /*1.获取通道
         *管道--运输 Channel通道只负责传输数据、不直接操作数据的。操作数据都是通过Buffer缓冲区来进行操作
         */
        FileChannel readChannel=read.getChannel();
        FileChannel writeChannel=write.getChannel();
        //2.分配指定大小的缓冲区
        ByteBuffer byteBuffer=ByteBuffer.allocate(1024*1024);//1M缓存区,承载数据
        //3.将通道里的数据存入缓冲池
        while(readChannel.read(byteBuffer)>0) {
            byteBuffer.flip();//切换成读模式
            //4将缓冲池中的数据写入通道中
            writeChannel.write((byteBuffer));//使用内存映射文件的方式实现文件复制的功能(直接操作缓冲区)：
            byteBuffer.clear();//清空缓冲区
        }
        writeChannel.close();
        readChannel.close();
        long endTime=System.currentTimeMillis();
        return endTime - startTime;
    }

    public static void main(String[] args) throws IOException {
        SimpleFileTransferTest simpleFileTransferTest = new SimpleFileTransferTest();
        File source = new File("D:\\开发工具\\ideaIU-2019.2.3.exe");
        File des = new File("C:\\Users\\MLoong\\Desktop\\新建文件夹\\io.exe");
        File nio = new File("C:\\Users\\MLoong\\Desktop\\新建文件夹\\nio.exe");

        long time = simpleFileTransferTest.transferFile(source, des);
        System.out.println(time + "：普通字节流时间");

        long timeNio = simpleFileTransferTest.transferFileWithNIO(source, nio);
        System.out.println(timeNio + "：NIO时间");
    }
}
