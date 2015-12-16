/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server_Side;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the thread that will manage multiple connections, will endlessly listen
 * In order to facilitate multiple client connections
 * this main server's only purpose is to continually listen for new connections and make them appropriately
 * @author devon
 */
public class AcceptingThread implements Runnable
{

    private ServerSocket ss;
    private int PORT = 5544;
    @Override
    public void run() 
    {
        try {
            ss = new ServerSocket(PORT);
        } catch (IOException ex) {
            Logger.getLogger(AcceptingThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while(true)
        {
            try {
                Socket clientsocket = ss.accept();
                
            } catch (IOException ex) {
                Logger.getLogger(AcceptingThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
}
