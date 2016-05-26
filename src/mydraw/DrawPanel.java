package mydraw;

import javax.swing.*;
import java.awt.*;

/**
 * Klasse um den Navigations Panel zu händeln
 *
 * @author Sabrina Buczko und Tom Kastek (Gruppe 2)
 */
class DrawPanel extends JPanel {
	public DrawPanel() {
		super(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);


//		g.drawImage(
//				image, 0, 0, image.getWidth(), image.getHeight(), null
//		);
	}

	public void repaint(){
		super.repaint();
		Graphics g = this.getGraphics();

	}
}