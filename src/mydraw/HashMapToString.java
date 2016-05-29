package mydraw;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by iTom on 29.05.16.
 */
public class HashMapToString {
	public static void main(String[] args) {
		HashMap<String, String> testmap = new HashMap<String, String>();
		testmap.put("Hallo", "du");
		System.out.println(testmap.toString());
		String testString = testmap.toString();

		String value = testString.substring(1, testString.length() - 1);           //remove curly brackets
		String[] keyValuePairs = value.split(",");              //split the string to creat key-value pairs
		HashMap<String,String> map = new HashMap<String, String>();

		for(String pair : keyValuePairs)                        //iterate over the pairs
		{
			String[] entry = pair.split("=");                   //split the pairs to get key and value
			map.put(entry[0].trim(), entry[1].trim());          //add them to the hashmap and trim whitespaces
		}

		Color red = Color.red;
		System.out.println(red.toString());
	}
}
