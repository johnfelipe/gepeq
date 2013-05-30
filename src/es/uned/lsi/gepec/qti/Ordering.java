/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package es.uned.lsi.gepec.qti;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Clase que representa el criterio de ordenaci�n de los �tems de una secci�n de un Test seg�n 
 * la norma QTI del IMS. El �nico criterio definido por la norma es permitir mezclar los �tems
 * aleatoriamente o dejarlos en la posici�n definida en el Test. Contiene un atributo que se 
 * corresponde con el definido en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class Ordering {

	/**
	 * Variable indicando si se deben mezclar aleatoriamente los �tems o no.
	 */
	private boolean shuffle;
	
	/**
	 * Constructor que simplemente inicializa la variable de la clase boolean.
	 * 
	 * @param 	shuffle		boolean indicando si se mezclan aleatoriamente los �tems
	 */
	public Ordering (boolean shuffle) {
		this.shuffle = shuffle;
	}

	/**
	 * M�todo que escribe este objeto <code>Ordering</code> en el <code>XMLStreamWriter</code> 
	 * recibido como par�metro, seg�n la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay alg�n problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaOrdering(XMLStreamWriter xsw) throws XMLStreamException{
		
		xsw.writeEmptyElement("ordering");		// Escribe el elemento
		xsw.writeAttribute("shuffle", Boolean.toString(shuffle));	// Atributo del elemento
	}
}