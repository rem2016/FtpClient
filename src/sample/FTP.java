package sample;

import sun.net.ftp.FtpClient;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/6/13.
 */
public class FTP {
    private String serverAddress;
    private Socket commandSocket, dataSocket;
    private int local_port=2021, data_port;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private boolean loggined;

    private String command(String com){
        printWriter.write(com);
        printWriter.flush();
        String ans = "";
        try {
            ans = bufferedReader.readLine();
            System.out.println(ans);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    FTP(String address, String username, String psw){
        serverAddress = address;
        loggined = false;
        try {
            commandSocket = new Socket(serverAddress, 21, null, local_port);

            printWriter = new PrintWriter(new BufferedOutputStream(commandSocket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));

            String welcome = bufferedReader.readLine();
            System.out.println(welcome);

            command("USER " + username + "\r\n");
            String info = command("PASS " + psw + "\r\n");
            loggined = info.split(" ")[0].equals("230");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    FTP(String address){
        this(address, "anonymous", "");
    }

    private void passiveMode() throws IOException {
        String passiveMode = command("PASV\r\n");
        String ip;
        try {
            ip = passiveMode.substring(passiveMode.indexOf('('), passiveMode.indexOf(')'));
        }catch (StringIndexOutOfBoundsException e){
            passiveMode = bufferedReader.readLine();
            System.out.println(passiveMode);
            ip = passiveMode.substring(passiveMode.indexOf('('), passiveMode.indexOf(')'));
        }

        String[] ports = ip.split(",");
        data_port = Integer.valueOf(ports[4]) * 256 + Integer.valueOf(ports[5]);
        dataSocket = new Socket(serverAddress, data_port, null, local_port+1);
    }

    public boolean isAlive(){
        return loggined;
    }


    public List<String> listDir(){
        List<String> ss = new LinkedList<>();
        System.out.println("\n\n===========List============");
        try {
            passiveMode();
            command("LIST\r\n");
            BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream(), "GBK"));
            String s = dataReader.readLine();
            while(s != null) {
                int left_space = 2, i=1, size_begin=0, size_end=0, file_name_begin = 0;
                for(;i < s.length(); i++){
                    if(s.charAt(i) == ' ' && s.charAt(i-1) != ' ')left_space--;
                    if(left_space == 0)break;
                }
                for(;i < s.length(); i++){
                    if(size_begin == 0 && s.charAt(i) != ' '){
                        size_begin = i;
                    }
                    if(size_begin != 0 && size_end == 0 && s.charAt(i) == ' '){
                        size_end = i;
                    }
                    else if(size_end != 0 && s.charAt(i) != ' ' && s.charAt(i - 1) == ' '){
                        file_name_begin = i;
                    }
                }
                String size = s.substring(size_begin, size_end), name = s.substring(file_name_begin);
                System.out.println(size + " " + name);
                s = dataReader.readLine();
                if(!Objects.equals(size, "<DIR>"))
                    ss.add(name);
            }
            dataSocket.close();
            System.out.println(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ss;
    }
    public String[] getLocalFile(){
        File dir = new File("./");
        return dir.list();
    }

    public void upload(File uploadFile){
        try{
            passiveMode();
            command("STOR " + uploadFile.getName() + "\r\n");
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(dataSocket.getOutputStream());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(uploadFile));
            byte buf[] = new byte[10000];
            int n = 0;
            while((n = bufferedInputStream.read(buf)) != -1){
                bufferedOutputStream.write(buf, 0, n);
                bufferedOutputStream.flush();
            }
            bufferedInputStream.close();
            bufferedOutputStream.close();
            System.out.println(bufferedReader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void upload(String path){
        upload(new File(path));
    }

    public void download(String downloadFileName){
        try {
            passiveMode();
            command("RETR " + downloadFileName + "\r\n");
            File file = new File("./" + downloadFileName);
            byte b[] = new byte[10000];
            BufferedOutputStream fileWriter = new BufferedOutputStream(new FileOutputStream(file));
            BufferedInputStream dataReader = new BufferedInputStream(dataSocket.getInputStream());
            while(dataReader.read(b) != -1) {
                fileWriter.write(b);
                fileWriter.flush();
            }
            fileWriter.close();
            dataReader.close();
            System.out.println(bufferedReader.readLine());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String args[]){
        FTP ftp = new FTP("192.168.234.1");
        ftp.download("test.html");
        ftp.download("video.js");
        ftp.upload("test1.html");
        for(String i: ftp.getLocalFile())
            System.out.println(i);
    }
}
