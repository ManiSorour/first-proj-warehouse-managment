package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WarehouseClient {

    public static String givenCommand(String address , int port , String command){
        try (Socket socket = new Socket(address, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream());
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())))
        {

        out.println(command);
            String response = in.readLine();
            return response != null ? response : "(پاسخی از سرور دریافت نشد)";


        } catch (IOException e) {
            return "خطا در اتصال به سرور: " + e.getMessage();
        }

    }




}
