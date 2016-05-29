
package mydraw;

/**
* Unit test for Draw
* with exceptions (color)
*/

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

   @Test
   // check Size for drawings
   public void setgetSize() {
       try {
           draw.setHeight(650);
           draw.setWidth(1000);
           int h = draw.getHeight();
           assertEquals(650, h);
           assertEquals(1000, draw.getWidth());
       } catch (SizeException e) {
           System.out.println("Minimale Größe wurde versucht zu unterschreiten.");
       }
       try {
           draw.setWidth(300);
           fail("Minimale Größe konnte unterschritten werden");
       } catch (SizeException e) {
       }
       }

   @Test
   // set&check foreground colors for drawings
   public void setgetFGCol()  {
       assertEquals(draw.getFGColor(), "Black");
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
           MyBMPFile reader = new MyBMPFile();
           draw.autoDraw();
           Image img1, img2, img3;
           img2 = draw.getDrawing();
           draw.writeImage(img2, "bild1.bmp");
           img1 = draw.readImage("referenzbild.bmp");
           img3 = draw.readImage("bild1.bmp");
           assertEquals(img1.getWidth(reader), img2.getWidth(reader));
           assertEquals(img3.getHeight(reader), img2.getHeight(reader));
       } catch (IOException e) {
           System.out.println("Das erzeugte Bild stimmt nicht mit dem vorab geprüften Referenzbild überein");
       } catch (ColorException e) {
           System.out.println("Farbe im autoDraw falsch");
           e.printStackTrace();
       }
   }

    @Test
    // test, ob commands als txt gespeichert werden können.
    public void testWriteText(){
        try {
            draw.autoDraw();
            draw.writeText("writeExample.txt");
        } catch (TxtIOException e) {
        } catch (ColorException e) {
            e.printStackTrace();
        }
    }

    @Test
    // test, ob eine txt gelesen werden kann.
    public void testReadText(){
        try{
            draw.readText("autoDraw.txt");
        } catch (TxtIOException e){
        }
    }

    @Test
    // Undo / Redos werden getestet.
    public void testUndoRedo(){
        draw.undo();
        if(draw.window.undoItem.isEnabled()){
            fail("Undo should be disabled");
        }
        try {
            draw.autoDraw();
        } catch (ColorException e) {
            e.printStackTrace();
        }
        draw.undo();
        draw.undo();
        draw.undo();
        draw.redo();
        draw.redo();
        draw.redo();
        if(draw.window.redoItem.isEnabled()){
            fail("Redo should be disabled");
        }
    }

}