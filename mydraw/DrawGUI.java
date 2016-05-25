package mydraw;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Diese Klasse implementiert die GUI für unsere Applikation.
 *
 * @author Tom Kastek und Sabrina Buczko
 */
public class DrawGUI extends JFrame {
	Draw            app;                 // A reference to the application, to send commands to.
	Color           color;              // Speichert die Farbe in der gemalt werden soll.
	String 			colorName;		   // colorName wird in einem extra String zur Abfrage gespeichert
	Container       cp;               // Dadrauf werden die einzelnen Panels abgelegt
	NavigationPanel navigationPanel; // Hier werden die Buttons für Funktionen abgelegt
	protected DrawPanel drawPanel;	// Der Panel auf dem das Bild gemalt wirt
	BufferedImage image = (
			new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB)
	);	// Image jederzeit mit abspeichern, um es später einfach auszulesen
	HashMap<String, Color> colorMap = new HashMap<String, Color>(); // Map zu Farbauswahl
	JComboBox shape_chooser; // Zur Auswahl des Zeichenwerkzeuges
	JComboBox color_chooser; // Zur Auswahl der Farbe des Zeichenwerkzeuges
	CommandQueue commandQueue;

	/**
	 * Der GUI Konstruktor macht all die Arbeit um eine GUI zu erstellen
	 * und den Listener zu setzen.
	 */
	public DrawGUI(Draw application) {
		super("Draw");        // Create the window
		app = application;    // Remember the application reference
		commandQueue = new CommandQueue();

		setShapeChooser();
		fillColorMap();
		setColorChooser();

		JButton clear = new JButton("Clear");    // Button, der das Bild löschen soll
		JButton quit = new JButton("Quit");        // Button, der das Programm beenden soll.
		JButton auto = new JButton("Auto"); // Button, der ein autom. Bild malt
		
		

		// Setze das Layout für unser Fenster
		cp = this.getContentPane();
		cp.setLayout(new BorderLayout());

		setNavigationPanel(quit, clear, auto);

		// Setzt den Panel, auf dem gemalt wird
		drawPanel = new DrawPanel();

		// Setzt die Panels auf unseren ContentPane
		cp.add(navigationPanel, BorderLayout.NORTH, 0);
		cp.add(drawPanel, BorderLayout.CENTER, 1);

		// Lokale Klasse, um einen ActionListener für die Buttons zu haben.
		class DrawActionListener implements ActionListener {
			String command;

			public DrawActionListener(String cmd) {
				command = cmd;
			}

			public void actionPerformed(ActionEvent e) {
				if (command.equals("clear")) {
					Graphics g = drawPanel.getGraphics();
					g.setColor(Color.white);
					g.fillRect(0, 0, getWidth(), getHeight());
					drawPanel.paintComponents(g);
				} else if (command.equals("quit")) {
					app.window.dispose();
					System.exit(0);
				} else if (command.equals("auto")) {
					try {
						app.autoDraw();
					} catch (ColorException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		// Definiert ActionListener Adapter um die Buttons mit der Applikation zu verbinden
		clear.addActionListener(new DrawActionListener("clear"));
		quit.addActionListener(new DrawActionListener("quit"));
		auto.addActionListener(new DrawActionListener("auto"));

		// Lokale Klasse, die Maus Events verarbeitet,
		// abhängig vom aktuellem Shape.
		class ShapeManager implements ItemListener {
			DrawGUI gui;
			HashMap<String, ShapeDrawer> shapeMap = new HashMap<String, ShapeDrawer>();

			abstract class ShapeDrawer
					extends MouseAdapter implements MouseMotionListener {
				public void mouseMoved(MouseEvent e) { /* ignorieren */ }
			}

			// Wenn diese Klasse aktiv ist, wird die Maus als Stift interpretiert
			class ScribbleDrawer extends ShapeDrawer {
				int lastx, lasty;
				CommandQueue.Scribble line;
				Graphics2D g2D = image.createGraphics();

				public void mousePressed(MouseEvent e) {
					line = new CommandQueue.Scribble(color);
					lastx = e.getX();
					lasty = e.getY();
					line.addPoint(new Point(e.getX(), e.getY()));
					g2D.setColor(color);
				}

				public void mouseDragged(MouseEvent e) {
					Graphics g = drawPanel.getGraphics();
					int x = e.getX(), y = e.getY();
					g.setColor(gui.color);
					g.setPaintMode();
					g.drawLine(lastx, lasty, x, y);
					g2D.drawLine(lastx, lasty, x, y);
					line.addPoint(new Point(x, y));
//					drawPanel.repaint();
					lastx = x;
					lasty = y;
				}
				
				public void mouseReleased(MouseEvent e) {
					commandQueue.addScribble(line);
					commandQueue.drawLast(drawPanel.getGraphics());
				}
			}

			// Wenn diese Klasse aktiviert ist, werden Dreiecke gezeichnet
			class RectangleDrawer extends ShapeDrawer {
				int pressx, pressy;
				int lastx = -1, lasty = -1;
				Graphics2D g2D;
				

				// Maus gedrückt => setzt den ersten Punkt des Dreiecks
				public void mousePressed(MouseEvent e) {
					pressx = e.getX();
					pressy = e.getY();
					g2D = image.createGraphics();
					g2D.setColor(color);
				}

				// Maus losgelassen => Setzt zweiten Punkt des Viereck
				// und malt das resultierende Bild
				public void mouseReleased(MouseEvent e) {
					Graphics g = drawPanel.getGraphics();
					if (lastx != -1) {
						g.setXORMode(gui.color);
						g.setColor(gui.getBackground());
						doDraw(pressx, pressy, lastx, lasty, g);
						lastx = -1;
						lasty = -1;
					}
//					g.setPaintMode();
//					g.setColor(gui.color);
//					// Zeichne das finale Viereck
//					doDraw(pressx, pressy, e.getX(), e.getY(), g);
					commandQueue.addRectangle(gui.color, pressx, pressy, e.getX(), e.getY());
					commandQueue.drawLast(g);
					g2D.dispose();
					g2D = null;
				}

				// Maus in Bewegung => Setzt temporären zweiten Punkt des Dreiecks
				// Zeichnet das resultierende Bild vorübergehend
				public void mouseDragged(MouseEvent e) {
					Graphics g = drawPanel.getGraphics();
					g.setXORMode(gui.color);
					g.setColor(gui.getBackground());
					if (lastx != -1) {
						doDraw(pressx, pressy, lastx, lasty, g);

					}
					lastx = e.getX();
					lasty = e.getY();
					// Zeichne neues temporäres Dreieck
					doDraw(pressx, pressy, lastx, lasty, g);
				}

				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					// Kalkuliere Punkt, der oben links ist und setze Höhe und Breite
					int x = Math.min(x0, x1);
					int y = Math.min(y0, y1);
					int w = Math.abs(x1 - x0);
					int h = Math.abs(y1 - y0);
					// Zeichne Dreieck
					g.drawRect(x, y, w, h);
					g2D.drawRect(x, y, w, h);
					drawPanel.paintComponents(g);
				}
			}

			//Wenn diese Klasse aktiv ist, werden ovale gezeichnet
			class OvalDrawer extends RectangleDrawer {
				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					int x = Math.min(x0, x1);
					int y = Math.min(y0, y1);
					int w = Math.abs(x1 - x0);
					int h = Math.abs(y1 - y0);
					// Zeichne Oval
					g.drawOval(x, y, w, h);
					g2D.drawOval(x, y, w, h);
					drawPanel.paintComponents(g);
				}
				
				public void mouseReleased(MouseEvent e) {
					Graphics g = drawPanel.getGraphics();
					if (lastx != -1) {
						g.setXORMode(gui.color);
						g.setColor(gui.getBackground());
						doDraw(pressx, pressy, lastx, lasty, g);
						lastx = -1;
						lasty = -1;
					}
					commandQueue.addOval(gui.color, pressx, pressy, e.getX(), e.getY());
					commandQueue.drawLast(g);
					g2D.dispose();
					g2D = null;
				}
			}

			ScribbleDrawer  scribbleDrawer = new ScribbleDrawer();
			RectangleDrawer rectDrawer     = new RectangleDrawer();
			OvalDrawer      ovalDrawer     = new OvalDrawer();
			ShapeDrawer currentDrawer;
			
			public ShapeManager(DrawGUI itsGui) {
				gui = itsGui;
				// default: scribble mode
				currentDrawer = scribbleDrawer;
				// activate scribble drawer
				drawPanel.addMouseListener(currentDrawer);
				drawPanel.addMouseMotionListener(currentDrawer);
				shapeMap.put("Scribble", scribbleDrawer);
				shapeMap.put("Rectangle", rectDrawer);
				shapeMap.put("Oval", ovalDrawer);
			}

			// Setze den Shape drawer zurück
			public void setCurrentDrawer(ShapeDrawer l) {
				if (currentDrawer == l) {
					return;
				}

				// deaktiviere letzten drawer
				drawPanel.removeMouseListener(currentDrawer);
				drawPanel.removeMouseMotionListener(currentDrawer);
				// aktiviere neuen drawer
				currentDrawer = l;
				drawPanel.addMouseListener(currentDrawer);
				drawPanel.addMouseMotionListener(currentDrawer);
			}

			// User wählt neuen Shape => Setzt den shape mode zurück
			public void itemStateChanged(ItemEvent e) {
				setCurrentDrawer(shapeMap.get(e.getItem()));
			}
		}

		shape_chooser.addItemListener(new ShapeManager(this));

		class ColorItemListener implements ItemListener {
			public ColorItemListener() {
				color = Color.black;
			}

			// User wählt neue Farbe => Speichert neue Farbe in der DrawGUI
			public void itemStateChanged(ItemEvent e) {
					color = colorMap.get(e.getItem());
					colorName = (String) e.getItem();
			}
		}

		color_chooser.addItemListener(new ColorItemListener());

		// Programm wird beendet, bei Schließung des Fensters
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				app.window.dispose();
				System.exit(0);
			}
		});
		
		this.addComponentListener(new ComponentAdapter() {  
			public void componentResized(ComponentEvent evt) {
	            Component c = (Component)evt.getSource();
	            Graphics g = drawPanel.getGraphics();
	            g.setColor(Color.white);
	            g.fillRect(0, 0, getWidth(), getHeight());
	            commandQueue.redraw(g);
	        }
		});

		// Am Ende wird Größe des Fensters gesetzt und angezeigt
		drawPanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

		this.pack();
		drawPanel.setBackground(Color.white);
		this.setVisible(true);
	}

	/*
	 * Setzt einen Panel, der Buttons in einer Leiste hat.
	 * 
	 * @param quit Ein Button, der zum schließen des Fensters zuständig ist
	 * @param clear Ein Button, der zum Löschen der Zeichnung zuständig ist.
	 */
	private void setNavigationPanel(JButton quit, JButton clear, JButton auto) {
		navigationPanel = new NavigationPanel(new FlowLayout());
		navigationPanel.add(new JLabel("Shape:"));
		navigationPanel.add(shape_chooser);
		navigationPanel.add(new JLabel("Color:"));
		navigationPanel.add(color_chooser);
		navigationPanel.add(quit);
		navigationPanel.add(clear);
		navigationPanel.add(auto);
		navigationPanel.setBackground(Color.gray);
	}
	
	private void redraw(Graphics g){
		
	}

	// Erstellet einen Selektierer um die Farbe auszuwählen
	private void setColorChooser() {
		color_chooser = new JComboBox();
		color_chooser.addItem("Black");
		color_chooser.addItem("Blue");
		color_chooser.addItem("Red");
		color_chooser.addItem("Green");
	}

	// Setzt JComboBox auf, mit der man die Zeichenart wählen können soll
	private void setShapeChooser() {
		shape_chooser =new JComboBox();
		shape_chooser.addItem("Scribble");
		shape_chooser.addItem("Rectangle");
		shape_chooser.addItem("Oval");
	}

	// Füllt die colorMap
	private void fillColorMap() {
		colorMap.put("Black", Color.black);
		colorMap.put("Green", Color.green);
		colorMap.put("Blue", Color.blue);
		colorMap.put("Red", Color.red);
		colorMap.put("White", Color.white);
	}

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

			g.drawImage(
					image, 0, 0, image.getWidth(), image.getHeight(), null
			);
		}
	}
}