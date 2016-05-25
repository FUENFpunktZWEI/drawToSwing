// Purpose.  Command design pattern - decoupling producer from consumer

package mydraw;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * simple test with command queue pattern
 * modified by ptp
 *
 */
public class CommandQueue {
	List<Drawable> queue = new ArrayList<Drawable>();

	class Rectangle implements Drawable {
    	Color color;
    	int x;
    	int y;
    	int w;
    	int h;
    	
    	public Rectangle (Color color, int x0, int y0, int x1, int y1){
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
	}
	
	public void addRectangle(Color color, int x0, int y0, int x1, int y1){
		Rectangle rc = new Rectangle(color, x0, y0, x1, y1);
		queue.add(rc);
	}
	
	public void addOval(Color color, int x0, int y0, int x1, int y1){
		Oval oval = new Oval(color, x0, y0, x1, y1);
		queue.add(oval);
	}
	
	public void addScribble(Scribble scribble){
		queue.add(scribble);
	}

    class Oval extends Rectangle{
		public Oval(Color color, int x0, int y0, int x1, int y1) {
			super(color, x0, y0, x1, y1);
		}
		
		public void draw(Graphics g) {
    		g.setPaintMode();
			g.setColor(color);
			g.drawOval(x, y, w, h);
		}
	}

    static class Scribble implements Drawable {
    	Color color;
    	LinkedList<Point> points = new LinkedList<Point>();
    	
    	public Scribble (Color color){
    		this.color = color;
    	}
    	
    	public void addPoint(Point p){
    		points.add(p);
    	}

		public void draw(Graphics g) {
			// Zeichne Dreieck
			g.setPaintMode();
			g.setColor(color);
			for (int x = 0; x < points.size() - 1; x++){
				g.drawLine((int) points.get(x).getX(), (int) points.get(x).getY(), (int) points.get(x + 1).getX(), (int) points.get(x + 1).getY());
			}
		}
	}
    
//    public static void workOffRequests( List<Drawable> queue ) {
//	for (Iterator<Drawable> it = queue.iterator(); it.hasNext(); )
//	    it.next().draw(new Graphics));
//    }
    
    public void drawLast(Graphics g){
    	queue.get(queue.size() -1).draw(g);
    }
    
    public void redraw(Graphics g){
    	for (Iterator<Drawable> it = queue.iterator(); it.hasNext(); ){
    		it.next().draw(g);
    	}
    	System.out.println("");

    }
    
    public static void main(String[] args){
    	
    }
}
