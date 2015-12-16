package Client_Side;

import Exceptions.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

 
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

///////////////////////////////////////////////////////////////////////////////
//
//  Author:         Mason Toy
//  Class name:     FTPUpload
//  Description:    
//      This class is used to handle the task of uploading a file.
//      
///////////////////////////////////////////////////////////////////////////////
public class FTPUpload extends SwingWorker<Void, Void> {
    
    private static final int    BUFFER_SIZE = 4096;
    
    private String host;
    private int port;
    private String username;
    private String password;
    
    private String uploadPath;
    private File upFile;
    
    public FTPUpload(String h, int p, String u, String pass, File f){
        
        host = h;
        port = p;
        username = u;
        password = pass;
        upFile = f;
        
    }
    
    @Override
    public Void doInBackground() throws IOException, FTPException {
        
        FTPUtil ftp = new FTPUtil(host,port,username,password);
        
        try{
            
            ftp = new FTPUtil(host,port,username,password);  
            ftp.uploadFile(upFile);
            
            FileInputStream input = new FileInputStream(upFile);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = - 1;
            long totalBytes = 0;
            int percentFinished = 0;
            long fileLength = upFile.length();
            
            while((bytes = input.read(buffer)) != -1) {
                
               ftp.writeFileBytes(buffer, 0, bytes);
               totalBytes = totalBytes + bytes;
               percentFinished = (int)(totalBytes * 100 / fileLength);
               setProgress(percentFinished);
               
            }
            
            input.close();
  
            ftp.finish();
        
        
        } catch (FTPException ex) {        
            JOptionPane.showMessageDialog(null, "Error uploading: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);           
            ex.printStackTrace();
            setProgress(0);
            cancel(true);     
        }  
        finally{
            ftp.disconnect();  
        }
        
        return null;
       
    }
    
    @Override
    public void done() {
        
        if (!isCancelled()) {
            
            JOptionPane.showMessageDialog(null,  "File has been Uploaded "
                    + "successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE); 
        }
    }
    
    
    
}
