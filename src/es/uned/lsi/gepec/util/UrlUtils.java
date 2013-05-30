package es.uned.lsi.gepec.util;

/** 
 * Clase con métodos para codificar URLs. <br />
 * -----------------------------------------------------------------------------
 * URL Utils - UrlUtils.java
 * Author: C. Enrique Ortiz
 * Copyright (c) 2004-2005 C. Enrique Ortiz 
 *
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * Usage & redistributions of source code must retain the above copyright notice.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should get a copy of the GNU Lesser General Public License from
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------<br />
 * 
 * Modificado por David Domínguez. <br />
 * 
 * La clase contiene un método estático para codificar todos los caracteres, excepto los de 
 * tipo <code>mark</code> y los permitidos, de un <code>String</code> que representa una URL. Se 
 * ha añadido un método similar pero que no codifica tampoco los caracteres reservados.
 * 
 * @author	C. Enrique Ortiz
 * @author	David Domínguez
 * 
 */
public class UrlUtils {

    /**
     * Caracteres marcas no reservadas
     */
    private static String mark = "-_.!~*'()\"";
    /**
     * Caracteres reservados
     */
    private static String reservados = ";/?:@&=+$,";

    /**
     * Converts Hex digit to a UTF-8 "Hex" character.
     * 
     * @param digitValue digit to convert to Hex
     * @return the converted Hex digit
     */
    static private char toHexChar(int digitValue) {
        if (digitValue < 10)
            // Convert value 0-9 to char 0-9 hex char
            return (char)('0' + digitValue);
        else
            // Convert value 10-15 to A-F hex char
            return (char)('A' + (digitValue - 10));
    }

    /**
     * Encodes a URL - This method assumes UTF-8
     * Codifica todos los caracteres especiales, incluidos los caracteres reservados. Se 
     * utiliza para codificar nombres individuales de archivos o directorios de una URL.
     * Método original de la clase.
     * 
     * @param url URL to encode
     * @return the encoded URL
     */
    static public String encodeURLReserved(String url) {
        StringBuffer encodedUrl = new StringBuffer(); // Encoded URL
        int len = url.length();
        // Encode each URL character
        for(int i = 0; i < len; i++) {
            char c = url.charAt(i); // Get next character
            if ((c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z'))
                // Alphanumeric characters require no encoding, append as is
                encodedUrl.append(c);
            else {
                int imark = mark.indexOf(c);
                if (imark >=0) {
                    // Las marcas de puntuación no reservadas, los símbolos y los
                	// caracteres reservados no quieren codificación, se añaden 
                	// tal y como son
                    encodedUrl.append(c);
                } else {
                    // Encode all other characters to Hex, using the format "%XX",
                    //  where XX are the hex digits
                    encodedUrl.append('%'); // Add % character
                    // Encode the character's high-order nibble to Hex
                    encodedUrl.append(toHexChar((c & 0xF0) >> 4));
                    // Encode the character's low-order nibble to Hex
                    encodedUrl.append(toHexChar (c & 0x0F));
                }
            }
        }
        return encodedUrl.toString(); // Return encoded URL
    }
    
    /**
     * Encodes a URL - This method assumes UTF-8
     * Codifica todos los caracteres excepto los permitidos, los reservados y los <code>mark
     * </code>. Se utiliza para codificar URLs totales o parciales para que no codifique los 
     * caracteres reservados, como los separadores, que se supone que son separadores.
     * Método añadido a la clase.
     * 
     * @param 	url 	string con la URL a codificar
     * @return 			string con la URL codificada
     */
    static public String encodeURL(String url) {
        StringBuffer encodedUrl = new StringBuffer(); // Encoded URL
        int len = url.length();
        // Encode each URL character
        for(int i = 0; i < len; i++) {
            char c = url.charAt(i); // Get next character
            if ((c >= '0' && c <= '9') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z'))
                // Alphanumeric characters require no encoding, append as is
                encodedUrl.append(c);
            else {
            	String caracteres = mark + reservados;
                int imark = caracteres.indexOf(c);
                if (imark >=0) {
                    // Las marcas de puntuación no reservadas, los símbolos y los
                	// caracteres reservados no quieren codificación, se añaden 
                	// tal y como son
                    encodedUrl.append(c);
                } else {
                    // Encode all other characters to Hex, using the format "%XX",
                    //  where XX are the hex digits
                    encodedUrl.append('%'); // Add % character
                    // Encode the character's high-order nibble to Hex
                    encodedUrl.append(toHexChar((c & 0xF0) >> 4));
                    // Encode the character's low-order nibble to Hex
                    encodedUrl.append(toHexChar (c & 0x0F));
                }
            }
        }
        return encodedUrl.toString(); // Return encoded URL
    }
}