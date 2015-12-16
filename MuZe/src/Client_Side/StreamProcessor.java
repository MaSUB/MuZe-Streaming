/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client_Side;


import RTSPtest.Mp3Player;
import RTSPtest.RTPpacket;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.swing.JOptionPane;
import javazoom.jl.decoder.JavaLayerException;

/**
 *
 * @author devon
 */
public class StreamProcessor implements Runnable
{
    //thread variables
    Thread t;
    String threadname = "stream processor";
    //variables used for storing temporary files
    private final String Directory_Archive = "./temp";
    private final String Temp_mp3_archive = "music.mp3";
    private boolean isRunning;
    private int state;
    
    private final static int PLAYING = 0;
    private final static int PAUSED = 1;

    //variables used for streaming
    private final DatagramSocket RTPsocket;                 //socket to receive files
    private DatagramPacket rcvdp;                           //datagram packet from UDP connection
    private final byte[] buf;                               //buffer of data used when receiving packets
    private FileOutputStream fileoutput;
    
    
    Mp3Player player;                                       //the actual object to play the mp3 files, uses external library
    private long actualsize;                         //checks how much has been read so far
    FloatControl volumecontrol;
    public StreamProcessor(DatagramSocket s) throws FileNotFoundException
    {
        RTPsocket = s;
        buf = new byte[15000];                              //15kb is the buffer size
        actualsize = 0;
       
        fileoutput = new FileOutputStream(Directory_Archive +Temp_mp3_archive);
       
    }   
    @Override
    public void run() 
    {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
      
        
        while((player == null || !player.isComplete()) && isRunning){
            try {
                rcvdp = new DatagramPacket(buf, buf.length);
                
                if (player == null || (player != null && !player.isComplete()))
                {
                RTPsocket.receive(rcvdp);                                                   //receive the DP from the socket:
                }
                
                RTPpacket rtp_packet = new RTPpacket(rcvdp.getData(), rcvdp.getLength());   //create an RTPpacket object from the DP
                
                //print important header fields of the RTP packet received:
                //System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());
                
                
                //rtp_packet.printheader();                                                   //print header bitstream:
                
                //get the payload bitstream from the RTPpacket object
                int payload_length = rtp_packet.getpayload_length();
                byte [] payload = new byte[payload_length];
                rtp_packet.getpayload(payload);
                
                
                
                
                actualsize += payload.length;
                fileoutput.write(payload);
                fileoutput.flush();
                
                
                
                if((player == null && actualsize >=   20000) ||( (player != null) && (player.isComplete() && actualsize > 15000)))
                {
                    
                    player = new Mp3Player(Directory_Archive +Temp_mp3_archive);
                    player.play();
                    
                    

                }   } catch (IOException ex) {
                //Logger.getLogger(StreamProcessor.class.getName()).log(Level.SEVERE, null, ex);
                
            }
            
          
        
        }                 //loop while the player hasn't been initialized or completed streaming yet
        //File f = new File(Directory_Archive, Temp_mp3_archive);
        try {
                fileoutput.close();
                //f.delete();
            } catch (IOException ex) {
                //Logger.getLogger(StreamProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            player = null;
        
    }
    
    public void start()
    {
        if (t == null)
        {
            t = new Thread(this, threadname);
            t.start();
            isRunning = true;
            state = PLAYING;
        }
    }
    
    public void pause() throws InterruptedException
    {
        if (player != null)
        {
            player.pauseToggle();
            if(state == PLAYING)
            {
                state = PAUSED;
               
            }
            else if (state == PAUSED)
            {
                state = PLAYING;
                
            }
            
        }
        
    }
    public void join() throws InterruptedException
    {
        t.join();
    }
    
    public void kill() 
    {
        player.stop();
        isRunning = false;
        actualsize = 0;
      
    }
}
