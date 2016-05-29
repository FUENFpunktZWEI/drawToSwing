// Purpose.  Command design pattern - decoupling producer from consumer

package mydraw;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

/**
 * simple test with command queue pattern
 * modified by ptp
 */
public class CommandQueue {
	List<Drawable> queue     = new ArrayList<Drawable>(); // Queue to safe commands.
	List<Drawable> undoQueue = new ArrayList<Drawable>(); // Queue for re-do

	// Setzt die Quees auf null.
	public void clear() {
		queue = new ArrayList<Drawable>();
		undoQueue = new ArrayList<Drawable>();
	}

	/**
	 * Methode, um aus einer Farbe den String zu erhalten
	 *
	 * @param c Farbe, die als String ausgegeben werden soll.
	 */
	public String colorToString(Color c) {
		if (c.equals(Color.red)) {
			return "Red";
		} else if (c.equals(Color.black)) {
			return "Black";
		} else if (c.equals(Color.blue)) {
			return "Blue";
		} else if (c.equals(Color.white)) {
			return "White";
		} else if (c.equals(Color.green)) {
			return "Green";
		} else {
			return "No Color";
		}
	}

	// Klasse, die Rechtecke abbildet
	class Rectangle implements Drawable {
		Color color;
		int   x;
		int   y;
		int   w;
		int   h;

		public Rectangle(Color color, int x0, int y0, int x1, int y1) {
			this.color = color;
			x = Math.min(x0, x1);
			y = Math.min(y0, y1);
			w = Math.abs(x1 - x0);
			h = Math.abs(y1 - y0);
		}

		public void draw(Graphics g) {
			// Zeichne Dreieck
			g.setPaintMode();
			g.setColor(color);
			g.drawRect(x, y, w, h);

		}

		public String toString() {
			HashMap<String, String> hs = new HashMap<String, String>();
			hs.put("form", "rectangle");
			hs.put("color", colorToString(color));
			hs.put("x", Integer.toString(x));
			hs.put("y", Integer.toString(y));
			hs.put("w", Integer.toString(w));
			hs.put("h", Integer.toString(h));
			return hs.toString();
		}
	}

	// Klasse, die gefüllte Rechtecke abbildet
	class RectangleFilled extends Rectangle {
		public RectangleFilled(Color color, int x0, int y0, int x1, int y1) {
			super(color, x0, y0, x1, y1);
		}

		@Override
		public void draw(Graphics g) {
			g.setPaintMode();
			g.setColor(color);
			g.fillRect(x, y, w, h);
		}

		public String toString() {
			HashMap<String, String> hs = new HashMap<String, String>();
			hs.put("form", "rectangleFilled");
			hs.put("color", colorToString(color));
			hs.put("x", Integer.toString(x));
			hs.put("y", Integer.toString(y));
			hs.put("w", Integer.toString(w));
			hs.put("h", Integer.toString(h));
			return hs.toString();
		}
	}

	// Klasse die Dreiecke abbildet
	class Triangle implements Drawable {
		Color color;
		int x1;
		int y1;
		int x2;
		int y2;
		int   px1;
		int   py1;
		int   px2;
		int   py2;
		int   px3;
		int   py3;

		public Triangle(Color color, int x0, int y0, int x1, int y1) {
			this.color = color;
			this.x1 = x0;
			this.y1 = y0;
			this.x2 = x1;
			this.y2 = y1;
			px1 = Math.min(x0, x1);
			py1 = Math.max(y1, y0);
			py2 = py1;
			px2 = px1 + Math.abs(x0 - x1);
			px3 = px1 + (Math.abs(x0 - x1) / 2);
			py3 = py1 - Math.abs(y0 - y1);
		}

		public void draw(Graphics g) {
			// Zeichne Dreieck
			g.setPaintMode();
			g.setColor(color);
			g.drawLine(px1, py1, px2, py2);
			g.drawLine(px2, py2, px3, py3);
			g.drawLine(px3, py3, px1, py1);

		}

		public String toString() {
			HashMap<String, String> hs = new HashMap<String, String>();
			hs.put("form", "triangle");
			hs.put("color", colorToString(color));
			hs.put("x1", Integer.toString(x1));
			hs.put("y1", Integer.toString(y1));
			hs.put("x2", Integer.toString(x2));
			hs.put("y2", Integer.toString(y2));
			return hs.toString();
		}
	}

	// fügt der Queue ein neues Rechteck hinzu
	public void addRectangle(Color color, int x0, int y0, int x1, int y1) {
		Rectangle rc = new Rectangle(color, x0, y0, x1, y1);
		queue.add(rc);
	}

	// fügt der Queue ein neues Oval hinzu
	public void addOval(Color color, int x0, int y0, int x1, int y1) {
		Oval oval = new Oval(color, x0, y0, x1, y1);
		queue.add(oval);
	}

	// fügt der Queue ein neues Dreieck hinzu
	public void addTriangle(Color color, int x0, int y0, int x1, int y1) {
		Triangle triangle = new Triangle(color, x0, y0, x1, y1);
		queue.add(triangle);
	}

	// fügt der Queue ein neues gefülltes Rechteck hinzu
	public void addRectangleFilled(Color color, int x0, int y0, int x1, int y1) {
		RectangleFilled rcf = new RectangleFilled(color, x0, y0, x1, y1);
		queue.add(rcf);
	}

	// fügt der Queue ein neues Scribble hinzu
	public void addScribble(Scribble scribble) {
		queue.add(scribble);
	}

	// // fügt der Queue ein neues gefülltes Oval hinzu
	public void addOvalFilled(Color color, int x0, int y0, int x1, int y1) {
		OvalFilled of = new OvalFilled(color, x0, y0, x1, y1);
		queue.add(of);
	}

	// fügt der Queue eine neue Linie hinzu
	public void addLine(Color color, int x1, int y1, int x2, int y2) {
		Scribble scribble = new Scribble(color);
		scribble.addPoint(new Point(x1, y1));
		scribble.addPoint(new Point(x2, y2));
		queue.add(scribble);
	}

	// Klasse, die Ovale abbildet.
	class Oval extends Rectangle {
		public Oval(Color color, int x0, int y0, int x1, int y1) {
			super(color, x0, y0, x1, y1);
		}

		public void draw(Graphics g) {
			g.setPaintMode();
			g.setColor(color);
			g.drawOval(x, y, w, h);
		}

		public String toString() {
			HashMap<String, String> hs = new HashMap<String, String>();
			hs.put("form", "oval");
			hs.put("color", colorToString(color));
			hs.put("x1", Integer.toString(x));
			hs.put("y1", Integer.toString(y));
			hs.put("x2", Integer.toString(w));
			hs.put("y2", Integer.toString(h));
			return hs.toString();
		}
	}

	// Klasse, die gefüllte Ovale abbildet.
	class OvalFilled extends Oval {
		public OvalFilled(Color color, int x0, int y0, int x1, int y1) {
			super(color, x0, y0, x1, y1);
		}

		public void draw(Graphics g) {
			g.setPaintMode();
			g.setColor(color);
			g.fillOval(x, y, w, h);
		}

		public String toString() {
			HashMap<String, String> hs = new HashMap<String, String>();
			hs.put("form", "ovalFilled");
			hs.put("color", colorToString(color));
			hs.put("x1", Integer.toString(x));
			hs.put("y1", Integer.toString(y));
			hs.put("x2", Integer.toString(w));
			hs.put("y2", Integer.toString(h));
			return hs.toString();
		}
	}

	// Klasse die Scribble abbildet.
	class Scribble implements Drawable {
		Color color;
		LinkedList<Point> points = new LinkedList<Point>();

		public Scribble(Color color) {
			this.color = color;
		}

		public void addPoint(Point p) {
			points.add(p);
		}

		public void draw(Graphics g) {
			// Zeichne freie Linie
			g.setPaintMode();
			g.setColor(color);
			for (int x = 0; x < points.size() - 1; x++) {
				g.drawLine((int) points.get(x).getX(), (int) points.get(x).getY(), (int) points.get(x + 1).getX(),
						(int) points.get(x + 1).getY());
			}
		}

		public String toString() {
			HashMap<String, Object> hs = new HashMap<String, Object>();
			hs.put("form", "scribble");
			hs.put("color", colorToString(color));
			for (int x = 0; x < points.size(); x++) {
				hs.put("x" + Integer.toString(x + 1), (int) points.get(x).getX());
				hs.put("y"+Integer.toString(x+1), (int) points.get(x).getY());
			}
			return hs.toString();
		}
	}

	/**
	 * Zeichnet das letzte der Queue hinzugefügte Objekt auf.
	 *
	 * @param g Graphics auf denen gemalt werden soll.
	 * @return true, da nun undo-Button wieder auswählbar sein soll.
	 */
	public boolean drawLast(Graphics g) {
		queue.get(queue.size() - 1).draw(g);
		undoQueue = new ArrayList<Drawable>();
		return true;
	}

	/**
	 * Malt das komplette Bild neu.
	 *
	 * @param g  Graphics auf denen das neue Bild gemalt werden soll.
	 */
	public void redraw(Graphics g) {
		for (Iterator<Drawable> it = queue.iterator(); it.hasNext(); ) {
			it.next().draw(g);
		}
	}

	// Undo löscht das zuletzt hinzugefügte Objekt aus der Queue.
	public boolean undo() {
		if(queue.isEmpty()){
			System.out.println("undo was used although it shouldnt be possible");
			return false;
		} else {
			undoQueue.add(queue.remove(queue.size() - 1));
			if(queue.isEmpty()){
				return false;
			} else return true;
		}
	}

	/**
	 * Redo Malt das zuletzt entfernte Objekt wieder auf.
	 *
	 * @param g Graphics auf denen das neueste Objekt weider gemalt werden soll.
	 */
	public boolean redo(Graphics g) {
		if(queue.isEmpty()) {
			System.out.println("red was used although the queue is empty");
			return false;
		} else {
			queue.add(undoQueue.remove(undoQueue.size() - 1));
			queue.get(queue.size() - 1).draw(g);
			if(undoQueue.isEmpty()){
				return false;
			} else return true;
		}
	}

	public static void main(String[] args) {

	}
}
