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
 * Clase que representa el criterio de selección de los ítems de una sección de un Test según 
 * la norma QTI del IMS. El único criterio definido por la norma es seleccionar un subconjunto 
 * del conjunto total de ítems de la sección. Contiene unos atributos que se correspondes con 
 * los definidos en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class Selection {
	
	/**
	 * Número de ítems a seleccionar del número total de ítems.
	 */
	private int seleccion;
	/**
	 * Si se pueden repetir los ítems escogiéndolos más de una vez o no.
	 */
	private boolean conReemplazamiento;

	/**
	 * Constructor de la clase que simplemente le da valores iniciales a las variables de la 
	 * clase.
	 * 
	 * @param 	seleccion			entero con el número de ítems a seleccionar del conjunto
	 * @param 	conReemplazamiento	boolean indicando si se puede escoger varias veces el mismo 
	 * 								ítem para formar el Test final
	 */
	public Selection (int seleccion, boolean conReemplazamiento) {
		this.seleccion = seleccion;
		this.conReemplazamiento = conReemplazamiento;
	}
	
	/**
	 * Método "get" que devuelve el número de ítems a seleccionar del conjunto de los ítems de 
	 * la sección.
	 * 
	 * @return		entero indicando el número de ítems a seleccionar
	 */
	public int getSeleccion() {
		return seleccion;
	}

	/**
	 * Método que escribe este objeto <code>Selection</code> en el <code>XMLStreamWriter</code> 
	 * recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaSelection (XMLStreamWriter xsw) throws XMLStreamException{
		xsw.writeEmptyElement("selection ");		// Escribe el elemento
		xsw.writeAttribute("select", Integer.toString(seleccion));	// Atributo selección
		// Atributo con reemplazamiento
		xsw.writeAttribute("withReplacement", Boolean.toString(conReemplazamiento));
	}
}