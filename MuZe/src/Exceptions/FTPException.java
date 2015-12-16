/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Exceptions;

/**
 *
 * @author Mason
 */
public class FTPException extends Exception {
    
    public FTPException(String error) {
        
        super(error);
        
    }
    
}
