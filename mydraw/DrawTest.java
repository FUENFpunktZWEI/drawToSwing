
package mydraw;

/**
* Unit test for Draw
* with exceptions (color)
*/

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.image.*;
import java.io.IOException;
import java.awt.*;

import static org.junit.Assert.*;

/**
* @author ptp
*
*/
@RunWith(JUnit4.class)
public class DrawTest {
   Draw draw;
 // ....

   public DrawTest() throws ColorException, IOException {
       super();
       //...
       init();
      // ...
   }

   @Before
   public void init() throws ColorException, IOException {
       draw = new Draw();
   }

   @Ignore
   @Test
   // check defaults
   public void setgetDefaults() {
       int h = draw.getHeight();
       int w = draw.getWidth();
       assertEquals(h, 600);
       assertEquals(draw.getFGColor(), "Black");
       assertEquals(w, 600);
       }

   @Test
   // set&check foreground colors for drawings
   public void setgetFGCol()  {
       try {
           draw.setFGColor("Red");
           assertEquals(draw.getFGColor(), "Red");
           draw.setFGColor("undef");
           fail("Exception not thrown");
       } catch (ColorException e) {
       System.out.println("undef sollte nicht erkannt werden. ColorException korrekt.");
       }
   }

   @Test
   // test autoDraw-Image against (no-)reference
   public void testCompareImg() {
       try{
       MyBMBFile reader = new MyBMBFile();
       draw.autoDraw();
       Image img1, img2, img3;
       img2 = draw.getDrawing();
       draw.writeImage(img2, "bild1.bmp");
       img1 = draw.readImage("referenzbild.bmp");
       img3 = draw.readImage("bild1.bmp");
       assertEquals(img1.getWidth(reader), img2.getWidth(reader));
       assertEquals(img3.getHeight(reader), img2.getHeight(reader));
       } catch (IOException e) {
       System.out.println("Das erzeugte Bild stimmt nicht mit dem vorab geprüften Referenzbild überein"); }
       catch (ColorException e) {
    	System.out.println("Farbe im autoDraw falsch");
		e.printStackTrace();
	}
   }

}