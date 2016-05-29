package mydraw;

import javax.swing.*;

/**
 * Eigene Exception falls nicht existierende Dateien gewählt werden
 *
 * @author Sabrina Buczko und Tom Kastekk
 */
class TxtIOException extends Exception {

	TxtIOException() {
		super("Versucht als nicht txt zu speichern");
	}
}