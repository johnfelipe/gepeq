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
 * Clase que representa el criterio de selecci�n de los �tems de una secci�n de un Test seg�n 
 * la norma QTI del IMS. El �nico criterio definido por la norma es seleccionar un subconjunto 
 * del conjunto total de �tems de la secci�n. Contiene unos atributos que se correspondes con 
 * los definidos en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class Selection {
	
	/**
	 * N�mero de �tems a seleccionar del n�mero total de �tems.
	 */
	private int seleccion;
	/**
	 * Si se pueden repetir los �tems escogi�ndolos m�s de una vez o no.
	 */
	private boolean conReemplazamiento;

	/**
	 * Constructor de la clase que simplemente le da valores iniciales a las variables de la 
	 * clase.
	 * 
	 * @param 	seleccion			entero con el n�mero de �tems a seleccionar del conjunto
	 * @param 	conReemplazamiento	boolean indicando si se puede escoger varias veces el mismo 
	 * 								�tem para formar el Test final
	 */
	public Selection (int seleccion, boolean conReemplazamiento) {
		this.seleccion = seleccion;
		this.conReemplazamiento = conReemplazamiento;
	}
	
	/**
	 * M�todo "get" que devuelve el n�mero de �tems a seleccionar del conjunto de los �tems de 
	 * la secci�n.
	 * 
	 * @return		entero indicando el n�mero de �tems a seleccionar
	 */
	public int getSeleccion() {
		return seleccion;
	}

	/**
	 * M�todo que escribe este objeto <code>Selection</code> en el <code>XMLStreamWriter</code> 
	 * recibido como par�metro, seg�n la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay alg�n problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaSelection (XMLStreamWriter xsw) throws XMLStreamException{
		xsw.writeEmptyElement("selection ");		// Escribe el elemento
		xsw.writeAttribute("select", Integer.toString(seleccion));	// Atributo selecci�n
		// Atributo con reemplazamiento
		xsw.writeAttribute("withReplacement", Boolean.toString(conReemplazamiento));
	}
}