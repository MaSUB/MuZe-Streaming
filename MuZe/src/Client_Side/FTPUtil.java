package Client_Side;

import Exceptions.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

///////////////////////////////////////////////////////////////////////////////
//
//  Author:         Mason Toy
//  Description:    
//      This class is used to connect the client to the FTP server. This class
//      handles, uploading, downloading, logging in to the FTP server,
//      connecting to the FTP server and correctly disconnecting as well.
//      
///////////////////////////////////////////////////////////////////////////////
public class FTPUtil {
 
    ///////////////////////////////////////////////////////////////////////////
    //
    //  These attributes are used to connect the client to the server.
    //  host represents the host Server's IP/Web address, the portNum is the
    //  port number, username and password are used to log into the server.
    //  
    ///////////////////////////////////////////////////////////////////////////
    private final String    host;
    private final int       portNum;
    private final String    username;
    private final String    password;
 
    ///////////////////////////////////////////////////////////////////////////
    //
    //  The FTPClient class is used from the apache.commons.net libray's this
    //  makes connecting to a server and creating a client FTP very easy to 
    //  understand and straight forward.
    //
    ///////////////////////////////////////////////////////////////////////////
    private FTPClient client = new FTPClient();
    private int reply;
    private InputStream inStream;
    private OutputStream outStream;
    private String local;
 
    ///////////////////////////////////////////////////////////////////////////
    //
    //  When creating a new instance of the FTPUtil, it needs to be passed the
    //  host ip/webaddress, port#, username and password. This is needed to
    //  connect to the server.
    //  
    ///////////////////////////////////////////////////////////////////////////
    public FTPUtil(String h, int p, String u, String pass) {
        
        this.host = h;
        this.portNum = p;
        this.username = u;
        this.password = pass;
        
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  connect:
    //      This method is used to connect the client/host machine to the FTP
    //      Server. The host, portNum, username, and password are needed in
    //      order for this to function without exception.
    //  
    ///////////////////////////////////////////////////////////////////////////
    public void connect() throws FTPException {
        
        try {
            
            client.connect(host, portNum);
            reply = client.getReplyCode();
            
            if (!FTPReply.isPositiveCompletion(reply)) {
                
                throw new FTPException("serve couldn't connect.");
                
            }
 
            boolean logIn;
            logIn = client.login(username, password);
            
            if (!logIn) {
                
                client.disconnect();
                throw new FTPException("Unable to login.");
                
            }
 
            client.enterLocalPassiveMode();
 
        } catch (IOException ex) {
            
            throw new FTPException("Error during IO: " + ex.getMessage());
            
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  getFileSize:
    //      This message asks the server for the file specified in the filePath 
    //      string's length of file. The client asks for the list of files in 
    //      the specified directory. 
    //      
    //      If the file attribute is set to be null after this, then the file
    //      may not exist or be located in the passed filePath.
    //
    //      IF the file does exist however, the FTPClient class can invoke a
    //      method that gets the size of the file. This needs to be in the 
    //      'long' attribute type since it could be an extremely large file. 
    //
    ///////////////////////////////////////////////////////////////////////////
    public long getFileSize(String filePath) throws FTPException {
        
        try {
            
            FTPFile file;
            file = client.mlistFile(filePath);
            
            if (file == null) {
                
                throw new FTPException("404 file not found.");
                
            }
            
            return file.getSize();
            
        } catch (IOException ex) {
            
            throw new FTPException("Unable to calculate file size: " 
                    + ex.getMessage());
            
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  downloadFile:
    //      downloading the file sends the file through a binary file type. If
    //      setting the file you wish to download can not be converted to a
    //      binary file, may god have mercy on your soul, but really it will
    //      throw an exception. This exception will just display the problem
    //      in a VERY brief message.
    //  
    //      the inStream is then set equal to the incoming file which is 
    //      determined by the downloadPath attribute, this is used for the
    //      servers location of the file to be downloaded.
    //      
    //      If the inStream turns up to be null. then either the file does not 
    //      exist or there was an error when sending the data from the server.
    //
    ///////////////////////////////////////////////////////////////////////////
    public void downloadFile(String downloadPath) throws FTPException {
        try {
 
            boolean success = client.setFileType(FTP.BINARY_FILE_TYPE);
            if (!success) {
                throw new FTPException("Unable to set binary type to file.");
            }
            client.connect(host, portNum);
            client.login(username, password);
            inStream = client.retrieveFileStream(downloadPath);
            
            
            if (inStream == null) {
                
                throw new FTPException("Unable to open inStream. File may not "
                                        + "be available or exist.");
                
            }
            
        } catch (IOException ex) {
            
            System.err.print(ex);
            
        }
    }
    
    public void uploadFile(File upFile) throws FTPException, IOException {
        
            try {
            client.connect(host, portNum);
            client.login(username, password);
            boolean success = client.setFileType(FTP.BINARY_FILE_TYPE);      
            
            if (!success) {
                throw new FTPException("Could not set binary file type.");
            }
            
            outStream = client.storeFileStream(upFile.getName());
             
        } catch (IOException ex) {
            throw new FTPException("Error uploading file: " + ex.getMessage());
        }
        
        
    }
    
    public void writeFileBytes(byte[] bytes, int offset, int length)
            throws IOException {
        outStream.write(bytes, offset, length);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  This method makes sure that the download or upload when finished 
    //  completely closes the inStream and is sure that the client is not
    //  currently working on a project, if it is then it will complete that
    //  command.
    //
    ///////////////////////////////////////////////////////////////////////////
    public void finish() throws IOException {
        
        if(inStream != null)
            inStream.close();
        if(outStream != null)
            outStream.close();
        client.completePendingCommand();
        client.disconnect();
        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  This ends the current connection that the client has to the server. If 
    //  the client is not connected to the server, do nothing.
    //  
    ///////////////////////////////////////////////////////////////////////////
    public void disconnect() throws FTPException {
        
        if (client.isConnected()) {
            
            try {
                if(!client.logout())
                    throw new FTPException("Unable to logout correctly.");
                client.disconnect();
                
                
            } catch (IOException ex) {
                
                throw new FTPException("Error when discontecting from server: "
                        + ex.getMessage());
                
            }
        }
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    //
    //  Simple getter for the InputStream.
    //
    ///////////////////////////////////////////////////////////////////////////
    public InputStream getInStream() {
        
        return inStream;
        
    }
    
    public String[] getFileList() throws FTPException{
        
        try {
            return client.listNames();
        } catch (IOException ex) {
            Logger.getLogger(FTPUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}



