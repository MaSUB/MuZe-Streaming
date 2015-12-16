/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server_Side;

import RTSPtest.MP3Object;
import RTSPtest.RTPpacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author devon
 */
public class StreamSender implements Runnable
{
    DatagramSocket RTPSocket;
    ArrayList<Socket> clients;
    
    MP3Object songfile;
    private int imagenb = 0;
    private int MPA_TYPE = 14;
    private static int FRAME_PERIOD = 10;
    private static int RTP_dest_port = 5544;

    private boolean isRunning;
    //thread variables
    Thread t;
    String threadname = "sending thread";
    private static StreamSender instance = null;
    private static int counter = 0;
    private StreamSender(ArrayList<Socket> s)
    {
        clients = s;
        
        try {
            RTPSocket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(StreamSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static StreamSender getInstance(ArrayList<Socket> s){
        
        if(instance == null) {
            
            instance = new StreamSender(s);
            counter++;
        }
        
        return instance;
        
    }
    
    public void setSong(MP3Object song)
    {
        songfile = song;
    }
            
    @Override
    public void run() 
    {
        //if the current image nb is less than the length of the video
        
        while ((imagenb < songfile.getSize()) && isRunning)
        {
        //update current imagenb
        
       
            try {
            //get next frame to send from the video, as well as its size
            //byte [] temp = songfile.getFrame(imagenb);
            //int image_length = video.getnextframe(temp);

            //Builds an RTPpacket object containing the frame
            RTPpacket rtp_packet = new RTPpacket(MPA_TYPE, imagenb, imagenb*FRAME_PERIOD, songfile.getFrame(imagenb), songfile.getFrame(imagenb).length);
          
            //get to total length of the full rtp packet to send
            int packet_length = rtp_packet.getlength();

            //retrieve the packet bitstream and store it in an array of bytes
            byte[] packet_bits = new byte[packet_length];
            rtp_packet.getpacket(packet_bits);

            //send the packet as a DatagramPacket over the UDP socket 
          
            for(int i = 0; i < clients.size(); i++)
            {
                Socket tempsocket = clients.get(i);
                InetAddress ClientIPAddr = tempsocket.getInetAddress();
                DatagramPacket senddp = new DatagramPacket(packet_bits, packet_length, ClientIPAddr, RTP_dest_port);
           
                RTPSocket.send(senddp);
          
            }
            //System.out.println("Send frame #"+imagenb);
            //print the header bitstream
            rtp_packet.printheader();

            //update GUI
            //label.setText("Send frame #" + imagenb);
            imagenb++;
          
            Thread.sleep(20);
            }
            catch(Exception ex)
            {
                System.out.println("Exception caught 1: "+ex);
                System.exit(0);
            }
        }
        
     
    
  }
    
    public void start()
    {
        if(t == null)
        {
            t = new Thread(this, threadname);
            isRunning = true;
            t.start();
        }
    }
    
    public void join() throws InterruptedException
    {
        t.join();
        
    }
    public void kill()
    {
        RTPSocket.close();
        isRunning = false;
        instance = null;
        counter--;
    }
    
    public static int getCounter(){
        
        return counter;
        
    }
    
}
