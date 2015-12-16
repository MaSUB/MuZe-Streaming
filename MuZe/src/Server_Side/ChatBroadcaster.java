/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server_Side;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class updates a list of connected clients on the current contents of the server's playlist
 * It will have a method to send the initial contents and also a method for each update that will be called 
 * when a file is added
 * @author devon
 */
public class ChatBroadcaster 
{
    private ServerSocket playlistsocket;
    private ArrayList<Socket> clients;
   
    
    public ChatBroadcaster() throws IOException
    {
        playlistsocket = new ServerSocket(540);
        clients = new ArrayList();
        AcceptingThread at = new AcceptingThread();
        at.start();
    }
    
    
    
    public void sendtoAll(String sendstring) throws IOException
    {
       
        for (int j= 0; j < clients.size(); j++)
        {
            Socket tempsocket = clients.get(j);
            BufferedWriter buffw = new BufferedWriter(new OutputStreamWriter(tempsocket.getOutputStream()));
            buffw.write(sendstring + "\n");
            buffw.flush();
        }
        
    }
    
    
    
    public class AcceptingThread implements Runnable
    {
        private Thread t;
        private String threadname;
       
        @Override
        public void run() 
        {
           ChatReadThread chatreadthread;
        
            while(true)
            {
                try {
                    Socket clientsocket = playlistsocket.accept();
                    clients.add(clientsocket);
                    chatreadthread = new ChatReadThread(clientsocket);
                    chatreadthread.start();
                
                } catch (IOException ex) {
                    Logger.getLogger(AcceptingThread.class.getName()).log(Level.SEVERE, null, ex);
                }
              
            }
           
        }
        
        public void start()
        {
            if (t == null)
            {
                threadname = "connection listening thread";
                t = new Thread(this, threadname);
                t.start();
                
            }
                
        }
    
    
    
    }
    
    public class ChatReadThread implements Runnable
    {
        private Socket s;
        private Thread t;
        private String threadname = "chat server reading thread";
        
        public ChatReadThread(Socket s)
        {
            this.s = s;
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    BufferedReader buff = new BufferedReader(new InputStreamReader(s.getInputStream()));
                
                    String message = buff.readLine();
                
                    sendtoAll(message);
                } catch (IOException ex)
                {
                    Logger.getLogger(ChatBroadcaster.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        public void start()
        {
            if (t == null)
            {
                t = new Thread(this, threadname);
                t.start();
            }
        }
        
    }
}
