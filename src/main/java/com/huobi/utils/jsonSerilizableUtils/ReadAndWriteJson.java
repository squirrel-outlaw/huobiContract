package com.huobi.utils.jsonSerilizableUtils;

import java.io.*;
import java.net.URL;

/**
 * @Description
 * @Author squirrel
 * @Date 18-10-9 上午9:57
 */
public class ReadAndWriteJson {

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件所在路径
     * @param input    字符串
     * @throws IOException 异常
     */
    public static void writeFile(String filePath, String input) throws IOException {
        //判断路径上的文件是否存在，如不存在，则新建不存在的路径和文件
        judeFileExists(filePath);
        FileWriter fw = new FileWriter(filePath);
        PrintWriter out = new PrintWriter(fw);
        out.write(input);
        out.println();
        fw.close();
        out.close();
    }

    /**
     * 读取文本文件内容
     *
     * @param filePath 文件所在路径
     * @return 文本内容
     * @throws IOException 异常
     */
    public static String readFile(String filePath) throws IOException {
        StringBuffer sb = new StringBuffer();
        readToBuffer(sb, filePath);
        return sb.toString();
    }

    public static String readFile(InputStream is) throws IOException {
        StringBuffer sb = new StringBuffer();
        readToBuffer(sb, is);
        return sb.toString();
    }

    /**
     * 将文本文件中的内容读入到buffer中
     *
     * @param buffer
     * @param filePath 文件路径
     * @throws IOException 异常
     */
    private static void readToBuffer(StringBuffer buffer, String filePath) throws IOException {
        //判断路径上的文件是否存在，如不存在，则新建不存在的路径和文件
        judeFileExists(filePath);

        InputStream is = new FileInputStream(filePath);
        String line; // 用来保存每行读取的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // 读取第一行
        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }
        reader.close();
        is.close();
    }

    //  通过InputStream将文本文件中的内容读入到buffer中
    private static void readToBuffer(StringBuffer buffer, InputStream is) throws IOException {
        String line; // 用来保存每行读取的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        line = reader.readLine(); // 读取第一行
        while (line != null) { // 如果 line 为空说明读完了
            buffer.append(line); // 将读到的内容添加到 buffer 中
            buffer.append("\n"); // 添加换行符
            line = reader.readLine(); // 读取下一行
        }
        reader.close();
        is.close();
    }

    /**
     * 获取项目所在文件夹的绝对路径
     */
    public static String getCurrentDirPath() {
        URL url = ReadAndWriteJson.class.getProtectionDomain().getCodeSource().getLocation();
        String path = url.getPath();
        if (path.startsWith("file:")) {
            path = path.replace("file:", "");
        }
        if (path.contains(".jar!/")) {
            path = path.substring(0, path.indexOf(".jar!/") + 4);
        }

        File file = new File(path);
        path = file.getParentFile().getAbsolutePath();
        return path;
    }

    /**
     * 判断路径上的文件是否存在，如不存在，则新建不存在的路径和文件
     */
    private static void judeFileExists( String filePath) {
        //判断路径上的文件是否存在，如不存在，则新建
        File file = new File(filePath);
        //判断文件的上级文件夹是否存在
        if (!file.getParentFile().exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
            }
            //如果文件夹存在，再判断该文件是否存在
        } else if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }
    }


}
