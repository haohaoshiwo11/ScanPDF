package cn.com.dyg.utils;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;



public class ScanUtil {
    public static ConcurrentLinkedQueue<File> folderMethod1(String path) {

        File file = new File(path);
        ConcurrentLinkedQueue<File> list = new ConcurrentLinkedQueue<>();
        //保存所有pdf文件的对象
        ConcurrentLinkedQueue<File> pdfList = new ConcurrentLinkedQueue<File>();
        //该路径对应的文件或文件夹是否存在
        if (file.exists()) {
            //如果该路径为---文件或空文件夹
            if (null == file.listFiles()) {
                if (file.getAbsolutePath().endsWith(".pdf"))
                    pdfList.add(file);
            }
            //如果该路径为非空文件夹
            else {
                //将该路径下的所有文件（文件或文件夹）对象加入队列
                list.addAll(Arrays.asList(file.listFiles()));
                //遍历该队列
                while (!list.isEmpty()) {
                    File firstF = list.poll();
                    //这里不论是文件夹还是文件，只需判断是否以“.pdf”结尾
                    if (firstF.getAbsolutePath().endsWith(".pdf"))
                        pdfList.add(firstF);

                    File[] files = firstF.listFiles();

                    if (null == files) {

                        continue;
                    }
                    for (File f : files) {
                        if (f.isDirectory()) {
                            list.add(f);
                        } else {
                            if (f.getAbsolutePath().endsWith(".pdf"))
                                pdfList.add(f);
                        }
                    }
                }
            }

        }
        return pdfList;
    }

    public static ConcurrentLinkedQueue<File> folderMethod2(String path) {


        LinkedList<File> list = new LinkedList<>();
        String[] sts = path.trim().split(";");
        for (int i = 0; i < sts.length; i++) {
            File file = new File(sts[i]);
            //该路径对应的文件或文件夹是否存在
            if (file.exists()) {
                //将该路径下的所有文件（文件或文件夹）对象加入队列
                list.addAll(Arrays.asList(file.listFiles()));
            }
        }
        //保存所有pdf文件的对象
        ConcurrentLinkedQueue<File> pdfList = new ConcurrentLinkedQueue<File>();
        //遍历该队列
        while (!list.isEmpty()) {
            File firstF = list.poll();
            //这里不论是文件夹还是文件，只需判断是否以“.pdf”结尾
            if (firstF.getAbsolutePath().endsWith(".pdf"))
                pdfList.add(firstF);

            File[] files = firstF.listFiles();

            if (null == files) {

                continue;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    //继续扫描子目录下的pdf
                    list.add(f);
                } else {
                    if (f.getAbsolutePath().endsWith(".pdf"))
                        pdfList.add(f);
                }
            }
        }
        return pdfList;
    }
}
