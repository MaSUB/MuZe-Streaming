/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//  Baby girl, talk to me.
//  Let me talk to you, let me 'buy you a drank'
package Client_Side;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 *
 * @author Mason
 */
public class TPane extends JPanel {
    
     private ImageIcon image;

    public TPane() {
        image = new ImageIcon(("IMG_0052.JPG"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image.getImage(), 0, 0, null); // see javadoc for more info on the parameters 
    }
    
}
