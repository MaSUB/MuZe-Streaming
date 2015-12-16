/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client_Side;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 *This is a thread which will continuously listen on the given socket
 * for strings with which to append to a given jtext area, in the context of this
 * project, this will function as the playlist and update as songs are added to the playlist
 * @author devon
 */
public class ChatManager implements Runnable
{
    private JLabel artistLabel;
    private JLabel songLabel;
    private JLabel albumLabel;
    
    private Thread t;
    private String threadname = "PlaylistManager";
    private boolean isRunning = true;                   //variable flag to use to "kill" the thread
    
    private Socket s;
    private BufferedReader buff;
    private BufferedWriter writer;
    private JTextArea textarea;
    private String username;

    ChatManager(String IP, JTextArea textarea, JLabel artistLabel, JLabel songLabel, JLabel albumLabel, String username) throws IOException
    {
        String serverhost = IP;
        InetAddress ip = InetAddress.getByName(serverhost);
        s = new Socket(ip, 540);
        this.textarea = textarea;
        this.artistLabel = artistLabel;
        this.songLabel = songLabel;
        this.albumLabel = albumLabel;
        this.username = username;
        buff = new BufferedReader(new InputStreamReader(s.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }
    @Override
    public void run() 
    {
        while(isRunning)
        {
            String line;
            
            try {
                line = buff.readLine();
                
                textarea.append(line+ "\n");
                if ("**".equals(line.substring(0,2)))               //if the code for tags has been given,
                {
                    String temp = line.substring(2);
                    
                    String delimiter = "+";
                    
                    StringTokenizer tk = new StringTokenizer(temp, delimiter);
                    if(tk.hasMoreTokens())
                        artistLabel.setText(tk.nextToken(delimiter));
                    else
                        artistLabel.setText(" ");
                    
                    if(tk.hasMoreTokens())
                        songLabel.setText(tk.nextToken(delimiter));
                    else
                        songLabel.setText(" ");
                    if(tk.hasMoreTokens())
                        albumLabel.setText(tk.nextToken(delimiter));
                    else
                        albumLabel.setText(" ");
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ChatManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
    public void start()
    {
        t = new Thread(this, threadname);
        t.start();
    }
    
    public void sendMessage(String message) throws IOException
    {
        writer.write(username+ ": " + message+"\n");
        writer.flush();
    }
    /*
        This is custom version of the deprecated thread.kill() function
    */
    public void kill()
    {
        isRunning = false;
    }
    
}
