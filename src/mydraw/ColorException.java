package mydraw;

/**
 * Eigene Exception bei Auswahl einer nicht existierenden Farbe
 *
 * @author Sabrina Buczko und Tom Kastek
 */
class ColorException extends Exception {

	ColorException() {
		super("Farbe nicht vorhanden");
	}
}