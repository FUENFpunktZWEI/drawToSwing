package mydraw;

/**
 * Eigene Exception bei Einstellung einer zu kleinen Größe
 *
 * @author Sabrina Buczko und Tom Kastekk
 */
class SizeException extends Exception {

	SizeException() {
		super("Größe zu klein");
	}
}