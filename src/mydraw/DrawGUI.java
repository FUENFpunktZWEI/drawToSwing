package mydraw;

import apple.laf.JRSUIConstants;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.*;

/**
 * Diese Klasse implementiert die GUI für unsere Applikation.
 *
 * @author Tom Kastek und Sabrina Buczko
 */
public class DrawGUI extends JFrame {
	Draw      app;                 // A reference to the application, to send commands to.
	Color     color;              // Speichert die Farbe in der gemalt werden soll.
	Color     newBGColor;  // saves the Background Color
	String    colorName;           // colorName wird in einem extra String zur Abfrage gespeichert
	Container cp;               // Dadrauf werden die einzelnen Panels abgelegt
	JMenuBar  jMenuBar; // Menu Bar, zur Auswahl aller Funktionen
	protected DrawPanel drawPanel;    // Der Panel auf dem das Bild gemalt wirt
	HashMap<String, Color> colorMap = new HashMap<String, Color>(); // Map zu Farbauswahl
	JComboBox    shape_chooser; // Zur Auswahl des Zeichenwerkzeuges
	JComboBox    color_chooser; // Zur Auswahl der Farbe des Zeichenwerkzeuges
	JComboBox    bgColor_chooser; // Zur Auswahl der Hintergrundfarbe
	MyBMPFile    myBMPFile; // Hilfsklasse, um Bilder als bmpFile zu speicher.
	CommandQueue commandQueue; // Hier werden alle Mal-Kommandos gespeichert.
	JMenuItem    undoItem, redoItem; // Items um letzte Aktion rückgängig und wieder zurück zu machen

	/**
	 * Der GUI Konstruktor macht all die Arbeit um eine GUI zu erstellen
	 * und den Listener zu setzen.
	 */
	public DrawGUI(Draw application) {
		super("Draw");        // Create the window
		app = application;    // Remember the application reference
		commandQueue = new CommandQueue();
		myBMPFile = new MyBMPFile();

		// Auswahlfunktionen aufsetzen
		setShapeChooser();
		fillColorMap();
		setColorChooser();
		setBGColorChooser();

		// JMenuBar aufsetzen und Items anhängen
		jMenuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem clearItem = new JMenuItem("Clear");
		JMenuItem quitItem = new JMenuItem("Quit");
		JMenuItem autoItem = new JMenuItem("Auto");
		JMenuItem saveItem = new JMenuItem("Save");
		undoItem = new JMenuItem("Undo");
		redoItem = new JMenuItem("Redo");
		JMenuItem writeCommandsItem = new JMenuItem("Save Commands");
		JMenuItem readCommandsItem = new JMenuItem("Read Commands");
		fileMenu.add(saveItem);
		fileMenu.add(undoItem);
		fileMenu.add(redoItem);
		fileMenu.add(clearItem);
		fileMenu.add(autoItem);
		fileMenu.add(writeCommandsItem);
		fileMenu.add(readCommandsItem);
		fileMenu.add(quitItem);
		jMenuBar.add(fileMenu);
		jMenuBar.add(new JLabel("Shape: "));
		jMenuBar.add(shape_chooser);
		jMenuBar.add(new JLabel("Color: "));
		jMenuBar.add(color_chooser);
		jMenuBar.add(new JLabel("BG Color: "));
		jMenuBar.add(bgColor_chooser);

		// Setze das Layout für unser Fenster
		cp = this.getContentPane();
		cp.setLayout(new BorderLayout());

		// Setzt den Panel, auf dem gemalt wird
		drawPanel = new DrawPanel();

		// Setzt die Panels auf unseren ContentPane
		//		cp.add(navigationPanel, BorderLayout.NORTH, 0);
		cp.add(jMenuBar, BorderLayout.NORTH, 0);
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
					commandQueue.clear();
					undoItem.setEnabled(false);
					redoItem.setEnabled(false);
				} else if (command.equals("quit")) {
					app.window.dispose();
					System.exit(0);
				} else if (command.equals("auto")) {
					try {
						app.autoDraw();
					} catch (ColorException e1) {
						e1.printStackTrace();
					}
				} else if (command.equals("save")) {
					saveImageAsBMB();
				} else if (command.equals("undo")) {
					undoItem.setEnabled(commandQueue.undo());
					Graphics g = drawPanel.getGraphics();
					redrawBGColor(g);
					redoItem.setEnabled(true);
				} else if (command.equals("redo")) {
					Graphics g = drawPanel.getGraphics();
					redoItem.setEnabled(commandQueue.redo(g));
					undoItem.setEnabled(true);
				} else if (command.equals("commandSave")) {
					try {
						commandSave();
					} catch (TxtIOException e1) {
						e1.printStackTrace();
					}
				} else if (command.equals("commandRead")) {
					try {
						commandRead();
					} catch (TxtIOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}

		// Definiert ActionListener Adapter um die Buttons mit der Applikation zu verbinden
		clearItem.addActionListener(new DrawActionListener("clear"));
		quitItem.addActionListener(new DrawActionListener("quit"));
		autoItem.addActionListener(new DrawActionListener("auto"));
		saveItem.addActionListener(new DrawActionListener("save"));
		undoItem.addActionListener(new DrawActionListener("undo"));
		redoItem.addActionListener(new DrawActionListener("redo"));
		writeCommandsItem.addActionListener(new DrawActionListener("commandSave"));
		readCommandsItem.addActionListener(new DrawActionListener("commandRead"));

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

				public void mousePressed(MouseEvent e) {
					line = new CommandQueue().new Scribble(color);
					lastx = e.getX();
					lasty = e.getY();
					line.addPoint(new Point(e.getX(), e.getY()));
				}

				public void mouseDragged(MouseEvent e) {
					Graphics g = drawPanel.getGraphics();
					int x = e.getX(), y = e.getY();
					g.setColor(gui.color);
					g.setPaintMode();
					g.drawLine(lastx, lasty, x, y);
					line.addPoint(new Point(x, y));
					//					drawPanel.repaint();
					lastx = x;
					lasty = y;
				}

				public void mouseReleased(MouseEvent e) {
					commandQueue.addScribble(line);
					undoItem.setEnabled(commandQueue.drawLast(drawPanel.getGraphics()));
					redoItem.setEnabled(false);
				}
			}

			// Wenn diese Klasse aktiviert ist, werden Vierecke gezeichnet
			class RectangleDrawer extends ShapeDrawer {
				int pressx, pressy;
				int lastx = -1, lasty = -1;


				// Maus gedrückt => setzt den ersten Punkt des Vierecks
				public void mousePressed(MouseEvent e) {
					pressx = e.getX();
					pressy = e.getY();
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
					commandQueue.addRectangle(gui.color, pressx, pressy, e.getX(), e.getY());
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
				}

				// Maus in Bewegung => Setzt temporären zweiten Punkt des Vierecks
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
					// Zeichne neues temporäres Viereck
					doDraw(pressx, pressy, lastx, lasty, g);
				}

				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					// Kalkuliere Punkt, der oben links ist und setze Höhe und Breite
					int x = Math.min(x0, x1);
					int y = Math.min(y0, y1);
					int w = Math.abs(x1 - x0);
					int h = Math.abs(y1 - y0);
					// Zeichne Viereck
					g.drawRect(x, y, w, h);
				}
			}

			class RectangleDrawerFilled extends RectangleDrawer {
				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					// Kalkuliere Punkt, der oben links ist und setze Höhe und Breite
					int x = Math.min(x0, x1);
					int y = Math.min(y0, y1);
					int w = Math.abs(x1 - x0);
					int h = Math.abs(y1 - y0);
					// Zeichne gefülltes Viereck
					g.fillRect(x, y, w, h);
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
					commandQueue.addRectangleFilled(gui.color, pressx, pressy, e.getX(), e.getY());
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
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
					//					drawPanel.paintComponents(g);
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
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
				}
			}

			class LineDrawer extends RectangleDrawer {
				// Maus losgelassen => Setzt zweiten Punkt der Linie
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
					commandQueue.addLine(gui.color, pressx, pressy, e.getX(), e.getY());
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
				}

				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					g.drawLine(x0, y0, x1, y1);
				}
			}

			class OvalDrawerFilled extends RectangleDrawer {
				public void doDraw(int x0, int y0, int x1, int y1, Graphics g) {
					int x = Math.min(x0, x1);
					int y = Math.min(y0, y1);
					int w = Math.abs(x1 - x0);
					int h = Math.abs(y1 - y0);
					// Zeichne Oval
					g.fillOval(x, y, w, h);
					//					drawPanel.paintComponents(g);
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
					commandQueue.addOvalFilled(gui.color, pressx, pressy, e.getX(), e.getY());
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
				}
			}

			class TriangleDrawer extends ShapeDrawer {
				int pressx, pressy;
				int lastx = -1, lasty = -1;


				// Maus gedrückt => setzt den ersten Punkt des Dreiecks
				public void mousePressed(MouseEvent e) {
					pressx = e.getX();
					pressy = e.getY();
				}

				// Maus losgelassen => Setzt zweiten Punkt des Dreiecks
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
					commandQueue.addTriangle(gui.color, pressx, pressy, e.getX(), e.getY());
					undoItem.setEnabled(commandQueue.drawLast(g));
					redoItem.setEnabled(false);
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

					int px1 = Math.min(x0, x1);
					int py1 = Math.max(y1, y0);
					int py2 = py1;
					int px2 = px1 + Math.abs(x0 - x1);
					int px3 = px1 + (Math.abs(x0 - x1) / 2);
					int py3 = py1 - Math.abs(y0 - y1);
					// Zeichne Dreieck
					g.drawLine(px1, py1, px2, py2);
					g.drawLine(px2, py2, px3, py3);
					g.drawLine(px3, py3, px1, py1);

				}
			}

			// Setzt alle Drawer
			ScribbleDrawer        scribbleDrawer        = new ScribbleDrawer();
			RectangleDrawer       rectDrawer            = new RectangleDrawer();
			OvalDrawer            ovalDrawer            = new OvalDrawer();
			TriangleDrawer        triangleDrawer        = new TriangleDrawer();
			RectangleDrawerFilled rectangleDrawerFilled = new RectangleDrawerFilled();
			OvalDrawerFilled      ovalDrawerFilled      = new OvalDrawerFilled();
			LineDrawer            lineDrawer            = new LineDrawer();
			ShapeDrawer currentDrawer;

			/**
			 * Der Konstruktor des ShapeManagers
			 *
			 * @param itsGui die Gui auf den die Shapes später angewendet werden
			 */
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
				shapeMap.put("Triangle", triangleDrawer);
				shapeMap.put("Filled Rectangle", rectangleDrawerFilled);
				shapeMap.put("Filled Oval", ovalDrawerFilled);
				shapeMap.put("Line", lineDrawer);
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

		class BGColorItemListener implements ItemListener {
			// User wählt neue Farbe => Speichert neue Farbe in der DrawGUI
			public void itemStateChanged(ItemEvent e) {
				Graphics g = drawPanel.getGraphics();
				newBGColor = colorMap.get(e.getItem());
				g.setPaintMode();
				g.setColor(newBGColor);
				g.fillRect(0, 0, getWidth(), getHeight());
				commandQueue.redraw(g);
			}
		}

		bgColor_chooser.addItemListener(new BGColorItemListener());

		// Listener, der prüft, wenn sich die Fenstergröße ändert.
		drawPanel.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				Graphics g = drawPanel.getGraphics();
				redrawBGColor(g);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

		undoItem.setEnabled(false);
		redoItem.setEnabled(false);
		drawPanel.setPreferredSize(new Dimension(500, 500));
		this.pack();
		colorName = "Black";
		drawPanel.setBackground(Color.white);
		newBGColor = Color.white;
		this.setVisible(true);
	}

	/**
	 * Liest, commands aus einer .txt File. Alle Befehle werden auf das aktuelle Bild angewandt.
	 * Die Datei sollte eine Zeile pro Kommando enthalten.
	 * Die Zeilen sehen für die verschiedenen Shapes beispielsweise so aus:
	 * Scribble: {color=Black, form=scribble, y1=78, x1=115, y2=78, x2=116, y3=78, x3=117}
	 * Rectangle: {form=rectangle, color=Black, w=41, x=116, h=23, y=118}
	 * Oval: {form=oval, color=Black, y1=171, x1=141, y2=12, x2=22}
	 * Triangle: {form=triangle, color=Black, y1=167, x1=235, y2=182, x2=252}
	 * filled Rectangle: {form=rectangleFilled, color=Black, w=30, x=311, h=41, y=190}
	 * filled Oval: {form=ovalFilled, color=Black, y1=219, x1=134, y2=13, x2=27}
	 * Line: {form=scribble, color=Black, y1=246, x1=159, y2=246, x2=210}
	 *
	 * @throws TxtIOException
	 */
	private void commandRead() throws TxtIOException {
		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setDialogTitle("select a command text file");
		int retrival = jFileChooser.showOpenDialog(null);
		if (retrival == JFileChooser.APPROVE_OPTION) {
			File file = jFileChooser.getSelectedFile();
			commandReadHelper(file);
		}

	}

	/**
	 * Hilfsklasse für commandRead(). Liest die einzelnen Zeilen und malt diese aufs Bild
	 *
	 * @param file Datei von der die Kommandos gelesen werden sollen
	 * @throws TxtIOException
	 */
	public void commandReadHelper(File file) throws TxtIOException {
		InputStream is = null;
		String line;
		try {
			is = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
			BufferedReader br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				String value = line.substring(1, line.length() - 1);           //remove curly brackets
				String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
				HashMap<String, String> map = new HashMap<String, String>();

				for (String pair : keyValuePairs)                        //iterate over the pairs
				{
					String[] entry = pair.split("=");                   //split the pairs to get key and value
					map.put(entry[0].trim(),
							entry[1].trim());          //add them to the hashmap and trim whitespaces
				}

				String form = map.get("form");
				if (form != null) {
					Color c = colorMap.get(map.get("color"));
					if (form.equals("rectangle")) {
						commandQueue.addRectangle(c, Integer.parseInt(map.get("x")), Integer.parseInt(
								map.get("y")), Integer.parseInt(map.get("w")), Integer.parseInt(map.get("h")));
					} else if (form.equals("oval")) {
						commandQueue.addOval(c, Integer.parseInt(map.get("x1")), Integer.parseInt(
								map.get("y1")), Integer.parseInt(map.get("x2")), Integer.parseInt(map.get("y2")));
					} else if (form.equals("ovalFilled")) {
						commandQueue.addOvalFilled(c, Integer.parseInt(map.get("x1")), Integer.parseInt(
								map.get("y1")), Integer.parseInt(map.get("x2")), Integer.parseInt(map.get("y2")));
					} else if (form.equals("rectangleFilled")) {
						commandQueue.addRectangleFilled(c, Integer.parseInt(map.get("x")), Integer.parseInt(
								map.get("y")), Integer.parseInt(map.get("w")), Integer.parseInt(map.get("h")));
					} else if (form.equals("line")) {
						commandQueue.addLine(c, Integer.parseInt(map.get("x1")), Integer.parseInt(
								map.get("y1")), Integer.parseInt(map.get("x2")), Integer.parseInt(map.get("y2")));
					} else if (form.equals("triangle")) {
						commandQueue.addTriangle(c, Integer.parseInt(map.get("x1")), Integer.parseInt(
								map.get("y1")), Integer.parseInt(map.get("x2")), Integer.parseInt(map.get("y2")));
					} else if (form.equals("scribble")) {
						CommandQueue.Scribble scribble = new CommandQueue().new Scribble(c);
						for (int i = 0; i < (map.size() / 2) - 1; i++) {
							String x = map.get("x" + Integer.toString(i + 1));
							String y = map.get("y" + Integer.toString(i + 1));
							int x0 = Integer.parseInt(x);
							int y0 = Integer.parseInt(y);
							scribble.addPoint(new Point(x0, y0));
						}
						commandQueue.addScribble(scribble);
					}
					undoItem.setEnabled(commandQueue.drawLast(drawPanel.getGraphics()));
					redoItem.setEnabled(false);
				} else {
					throw new TxtIOException();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Speichert die Kommandos des aktuellen Bildes in einer txt Datei.
	 * @throws TxtIOException
	 */
	private void commandSave() throws TxtIOException {
		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setDialogTitle("save commands as txt");
		int retrival = jFileChooser.showSaveDialog(this);
		if (retrival == JFileChooser.APPROVE_OPTION) {
			File f = jFileChooser.getSelectedFile();
			String path = f.getAbsolutePath();
			commandSaveHelper(path);
		}

	}

	/**
	 * Hilfsklasse für commandSave(). Abhängig vom Pfad übernimmt diese Klasse das speichern.
	 * @param path
	 * @throws TxtIOException
	 */
	public void commandSaveHelper(String path) throws TxtIOException {
		if (!path.endsWith(".txt")) {
			JDialog meinJDialog = new JDialog();
			meinJDialog.setTitle("Mein JDialog Beispiel");
			meinJDialog.setSize(200, 100);
			meinJDialog.setModal(true);
			meinJDialog.add(new JLabel("Bitte als .txt speichern"));
			meinJDialog.setVisible(true);
			throw new TxtIOException();
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path, "UTF-8");
		} catch (FileNotFoundException e) {
			throw new TxtIOException();
		} catch (UnsupportedEncodingException e) {
			throw new TxtIOException();
		}
		for (Drawable x : commandQueue.queue) {
			writer.println(x.toString());
		}
		writer.close();
	}

	/**
	 * Ändert die Hintergrundfarbe des Bildes.
	 *
	 * @param g Das Bild auf dem gemalt werden soll.
	 */
	private void redrawBGColor(Graphics g) {
		g.setPaintMode();
		g.setColor(newBGColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		commandQueue.redraw(g);
	}

	/**
	 * Speichert das aktuelle Bild als .bmp per File Chosser an einem gewünschten Ort.
	 */
	private void saveImageAsBMB() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Bild als bmp speichern ...");
		int retrival = chooser.showSaveDialog(this);
		if (retrival == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				myBMPFile.write(f.getAbsolutePath(), getDrawing());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Save as file: " + f.getAbsolutePath());
		}
	}

	// Erstellet einen Selektierer um die Farbe auszuwählen
	private void setColorChooser() {
		color_chooser = new JComboBox();
		color_chooser.addItem("Black");
		color_chooser.addItem("Blue");
		color_chooser.addItem("Red");
		color_chooser.addItem("Green");
		color_chooser.addItem("White");
	}

	// Erstellt einen Selektierer um die Hintergrundfarbe auszuwählen
	private void setBGColorChooser() {
		bgColor_chooser = new JComboBox();
		bgColor_chooser.addItem("White");
		bgColor_chooser.addItem("Black");
		bgColor_chooser.addItem("Blue");
		bgColor_chooser.addItem("Red");
		bgColor_chooser.addItem("Green");
	}

	// Setzt JComboBox auf, mit der man die Zeichenart wählen können soll
	private void setShapeChooser() {
		shape_chooser = new JComboBox();
		shape_chooser.addItem("Scribble");
		shape_chooser.addItem("Rectangle");
		shape_chooser.addItem("Oval");
		shape_chooser.addItem("Triangle");
		shape_chooser.addItem("Filled Rectangle");
		shape_chooser.addItem("Filled Oval");
		shape_chooser.addItem("Line");
	}

	// Füllt die colorMap
	private void fillColorMap() {
		colorMap.put("Black", Color.black);
		colorMap.put("Green", Color.green);
		colorMap.put("Blue", Color.blue);
		colorMap.put("Red", Color.red);
		colorMap.put("White", Color.white);
	}

	// Gibt das aktuelle Bild als Image aus.
	public Image getDrawing() {
		BufferedImage bfi = new BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bfi.getGraphics();
		g.setPaintMode();
		g.setColor(newBGColor);
		g.fillRect(0, 0, getWidth(), getHeight());
		commandQueue.redraw(g);
		return bfi;
	}

}