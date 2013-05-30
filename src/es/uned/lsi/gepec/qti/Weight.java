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

import es.uned.lsi.gepec.util.Identificador;

/**
 * Clase que representa un Peso de un ítem según la norma QTI del IMS. Se utiliza para aplicar 
 * pesos distintos a los distintos ítems que componen el Test a la hora de evaluarlos. Contiene 
 * unos atributos que se corresponden con los definidos en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class Weight {

	/**
	 * Identificador válido del peso.
	 */
	private Identificador identificador;
	/**
	 * Valor del peso que tiene el ítem dentro del Test.
	 */
	private double peso;
	
	/**
	 * Constructor que simplemente le da valores iniciales a las variables de la clase.
	 * 
	 * @param 	identificador	identificador con el identificador válido de la clase
	 * @param 	peso			double con el valor del peso del ítem en el Test
	 */
	public Weight (Identificador identificador, double peso) {
		this.identificador = identificador;
		this.peso = peso;
	}
	
	/**
	 * Método que escribe este objeto <code>Weight</code> en el <code>XMLStreamWriter</code> 
	 * recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaWeight (XMLStreamWriter xsw) throws XMLStreamException{
		xsw.writeStartElement("weight");				// Escribe el elemento
		xsw.writeAttribute("identifier", identificador.getIdentificador());	// Identificador
		xsw.writeCharacters(Double.toString(peso));		// Valor del peso 
		xsw.writeEndElement();							// Cierra "weight"
	}
}