/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client_Side;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *This is a thread which will continuously listen on the given socket
 * for strings with which to append to a given jtext area, in the context of this
 * project, this will function as the playlist and update as songs are added to the playlist
 * @author devon
 */
public class PlaylistManager implements Runnable
{
    private Thread t;
    private String threadname = "PlaylistManager";
    private boolean isRunning = true;                   //variable flag to use to "kill" the thread
    
    private Socket s;
    private BufferedReader buff;
    private JTextArea textarea;

    PlaylistManager(JTextArea textarea) throws IOException
    {
        String serverhost = "localhost";
        InetAddress ip = InetAddress.getByName(serverhost);
        s = new Socket(ip, 5545);
        this.textarea = textarea;
        buff = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }
    @Override
    public void run() 
    {
        while(isRunning)
        {
            String line;
            try {
                line = buff.readLine();
                textarea.append(line + "\n");
            } catch (IOException ex) {
                Logger.getLogger(PlaylistManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    public void start()
    {
        t = new Thread(this, threadname);
        t.start();
    }
    /*
        This is custom version of the deprecated thread.kill() function
    */
    public void kill()
    {
        isRunning = false;
    }
    
}
