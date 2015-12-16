package Client_Side;

import Exceptions.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
 
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;


///////////////////////////////////////////////////////////////////////////////
//
//  Author:         Mason Toy
//  Description:    
//      This class is used to handle the task of downloading a file, the
//      connection to the FTP server is handled by the FTPUtil class. Once
//      connected, the user then downloads the file to the provided file path.
//      
///////////////////////////////////////////////////////////////////////////////
public class FTPDownload extends SwingWorker<Void, Void> {
    
    private static final int    BUFFER_SIZE = 4096;
    
    private String              host;
    private int                 port;
    private String              username;
    private String              password;
    
    private String              dlPath;
    private String              savePath;
    DownloadGUI dlGui;
   
    //  The constructor needs to also initalize the GUI that needs done above.
    public FTPDownload(String h, int p, String u, String pass, String dl,
                        String sPath) {
       
        host        = h;
        port        = p;
        username    = u;
        password    = pass;
        dlPath      = dl;
        savePath    = sPath;
                
    }

    FTPDownload() {
        dlGui = new DownloadGUI();
    }
    
    @Override
    protected Void doInBackground() throws Exception {
        
        FTPUtil ftp = new FTPUtil(host,port,username,password);
        
        try {
            
            ftp.connect();
            
            byte[]              buffer = new byte[BUFFER_SIZE];
            int                 bytes = -1;
            long                totalBytes = 0;
            int                 percentDone = 0;
            long                fileLength = ftp.getFileSize(dlPath);
            
            String              fileName;
            File                dlFile;
            FileOutputStream    fileOut;
            InputStream         fileIn;  
            
            
            fileName    =   new File(dlPath).getName();
            dlFile      =   new File(savePath + File.separator + fileName);
            fileOut     =   new FileOutputStream(dlFile);
            
            ftp.downloadFile(dlPath);           
            fileIn      =   ftp.getInStream();
            
            ///////////////////////////////////////////////////////////////////
            //  
            //  As long as the file is not null (-1) you write to the new file
            //  that you have created. This will also update the progess that 
            //  is in the super class swingWorker. This will be used to show a 
            //  progressbar.
            //
            ///////////////////////////////////////////////////////////////////
            while((bytes = fileIn.read(buffer)) != -1) {
                
                fileOut.write(buffer,0 ,bytes);
                totalBytes = bytes + totalBytes;
                percentDone = (int) (totalBytes * 100 / fileLength);
                setProgress(percentDone);
                
            }
            
            
            ///////////////////////////////////////////////////////////////////
            //
            //  these two statements close the outputStream and the ftpClients
            //  connection since the file is now finished.
            //
            ///////////////////////////////////////////////////////////////////

            fileOut.close();
            fileIn.close();
            ftp.finish();
            
            
            ///////////////////////////////////////////////////////////////////
            //
            //  if there was any error during the downloading process, a panel
            //  displaying the error information will appear informing the user
            //  that the file was unable to be downloaded.
            //
            ///////////////////////////////////////////////////////////////////
        } catch(FTPException ex) {
            
            JOptionPane.showMessageDialog(null, "Error occured when downloading"
                    + " the file: " +ex, "Error Message" , 
                    JOptionPane.ERROR_MESSAGE);
            
            ex.printStackTrace();
            setProgress(0);
            cancel(true);
            
        }
        finally{  
        ftp.disconnect();
        }
        return null;

        
        
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  When finished with the download this will appear informing the user
    //  that the file was successfully downloaded.
    //
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void done() {
        
        if (!isCancelled()) {
            
            JOptionPane.showMessageDialog(null,  "File has been downloaded "
                    + "successfully." + "\nThe file path is:\t" + savePath
                    , "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            
        }
    } 
}
