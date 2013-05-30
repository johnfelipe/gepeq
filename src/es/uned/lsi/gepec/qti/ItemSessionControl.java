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
 * Clase que representa un objeto de control de la sesión al realizar el Test según la norma 
 * QTI del IMS. Contiene sólo una variable, que se corresponde con el atributo "allowReview" 
 * definido en la norma.
 * 
 * @author David Domínguez
 *
 */
public class ItemSessionControl {

	/**
	 * Permitir al candidato revisar la sección tras responderla con las respuestas 
	 * que ha dado.
	 */
	boolean allowReview;
	
	/**
	 * Constructor que establece el valor del único campo de la clase, <code>allowReview</code>.
	 * 
	 * @param 	allowReview		permitir o no revisar el examen tras responderlo
	 */
	public ItemSessionControl (boolean allowReview) {
		this.allowReview = allowReview;
	}
	
	/**
	 * Método que escribe este objeto <code>ItemSessionControl</code> en el <code>
	 * XMLStreamWriter</code> recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaItemSessionControl(XMLStreamWriter xsw) throws XMLStreamException{

		xsw.writeEmptyElement("itemSessionControl");	// Escribe el elemento vacío
		xsw.writeAttribute("allowReview", Boolean.toString(allowReview)); // Escribe el Atributo
	}
}