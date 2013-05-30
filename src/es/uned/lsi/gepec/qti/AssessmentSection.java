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

import java.io.File;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import es.uned.lsi.gepec.util.Identificador;
import es.uned.lsi.gepec.util.ParserNeutro;

/**
 * Clase que representa a una secci�n del Test seg�n la norma QTI del IMS. Se utiliza para 
 * agrupar varios �tems dentro de un Test. Contiene varios atributos que se corresponden con 
 * los atributos definidos en la norma, como son el t�tulo de la secci�n, si es visible, si se 
 * sigue alg�n criterio de selecci�n y ordenaci�n, y el array de objetos <code>
 * AssessmentItemRef</code> con las referencias de los �tems incluidos en la secci�n. Tal y 
 * como define la norma esta clase hereda de la clase "SectionPart", por lo tanto tambi�n 
 * contendr� sus variables de clase.

 * @author Blas Medina Alcudia
 *
 */
public class AssessmentSection extends SectionPart {

	/**
	 * T�tulo de la secci�n.
	 */
	private String titulo;
	/**
	 * Si la secci�n es visible para el candidato que realice el test.
	 */
	private boolean visible;
	/**
	 * Criterio de selecci�n de los �tems posibles a seguir.
	 */
	private Selection seleccion;
	/**
	 * Criterio de ordenaci�n de los �tems del test.
	 */
	private Ordering orden;
	/**
	 * Instrucciones del �tem. Es c�digo XHTML.
	 */
	private String instrucciones;
	/**
	 * Array de referencias a los �tems que se incluyen en la secci�n.
	 */
	private AssessmentItemRef[] itemRefArray;
	
	/**
	 * Constructor que inicializa variables de clase. Inicializa las siguientes variables de 
	 * clase: <code>titulo</code>, <code>visible</code>, <code>seleccion</code>, <code>orden
	 * </code>, e <code>instrucciones</code>, y llamando al constructor de la superclase con el 
	 * identificador e iniciando ambos atributos de secci�n requerida y fija a falso, ya que 
	 * esta implementaci�n de la norma QTI del IMS s�lo permite una secci�n dentro del test, 
	 * por lo que esos par�metros no tendr�n en cuenta.
	 * 
	 * @param 	identificador		identificador de la secci�n
	 * @param 	titulo				string con el t�tulo de la secci�n
	 * @param 	visible				boolean indicando la visibilidad de la secci�n
	 * @param 	seleccion			selection con el criterio de selecci�n de los �tems
	 * @param 	orden				ordering con el criterio de ordenaci�n de los �tems
	 * @param 	instrucciones		string con las instrucciones XHTML
	 */
	public AssessmentSection(Identificador identificador, String titulo, boolean visible, 
			Selection seleccion, Ordering orden, String instrucciones) {
		// Los par�metros requerido y fijo para la secci�n, como s�lo hay una, no son 
		// necesarios y no se tienen en cuenta
		super(identificador, false, false);
		this.titulo = titulo;
		this.visible = visible;
		this.seleccion = seleccion;
		this.orden = orden;
		this.instrucciones = instrucciones;
	}

	/**
	 * M�todo "get" para obtener el array de objetos de referencia a los �tems, <code>
	 * AssessmentItemRef</code>, de la secci�n.
	 * 
	 * @return		array de objetos AssessmentItemRef de la secci�n
	 * @see 		#setItemRefArray
	 */
	public AssessmentItemRef[] getItemRefArray() {
		return itemRefArray;
	}

	/**
	 * M�todo "set" para establecer el array de objetos de referencia a los �tems,<code>
	 * AssessmentItemRef</code>, de la secci�n.
	 *  
	 * @param 	itemRefArray	array de objetos AssessmentItemRef de la secci�n
	 * @see #getItemRefArray
	 */
	public void setItemRefArray(AssessmentItemRef[] itemRefArray) {
		this.itemRefArray = itemRefArray;
	}

	/**
	 * M�todo "get" para obtener el criterio de ordenaci�n de la secci�n.
	 * 
	 * @return		ordering con el criterio de ordenaci�n
	 */
	public Ordering getOrden() {
		return orden;
	}

	/**
	 * M�todo "get" para obtener el criterio de selecci�n de los �tems de la secci�n.
	 * 
	 * @return		selection con el criterio de selecci�n
	 */
	public Selection getSeleccion() {
		return seleccion;
	}
	
	/**
	 * Calcula la URI relativa de todos los �tems de la secci�n. El m�todo llama al m�todo para 
	 * calcular la URI relativa de un �tem para cada uno de los objetos referencia de �tem de 
	 * la secci�n, <code>AssessmentItemRef</code>, pas�ndole el objeto <code>File</code> 
	 * representando la direcci�n en disco del Test.
	 * 
	 * @param 	referencia		file con la direcci�n final en disco del Test
	 */
	public void creaRefItems (File referencia){
		for (int i = 0; i < itemRefArray.length; i++)
			itemRefArray[i].creaItemRef(referencia);
	}
	
	/**
	 * M�todo que escribe este objeto <code>AssessmentSection</code> en el <code>XMLStreamWriter
	 * </code> recibido como par�metro, seg�n la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay alg�n problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaAssessmentSection (XMLStreamWriter xsw) throws XMLStreamException{
		
		xsw.writeStartElement("assessmentSection");		// Comienzo del elemento
		xsw.writeAttribute("identifier", identificador.getIdentificador());	// Identificador
		xsw.writeAttribute("title", titulo);			// T�tulo de la secci�n
		xsw.writeAttribute("visible", Boolean.toString(visible));	// Atributo visible
		if (seleccion != null)							// Escribe la selecci�n si la hay
			seleccion.creaSelection(xsw);
		if (orden != null)								// Escribe el ordenaci�n si lo hay
			orden.creaOrdering(xsw);
		if (instrucciones != null) {					// Las instrucciones se escriben si hay
			xsw.writeStartElement("rubricBlock");		// Elemento para las instrucciones
			xsw.writeAttribute("view", "candidate");	// Las ve el candidato
			// Se escriben mediante el parser neutro ya que es c�digo XHTML
			ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeEndElement();						// Cierra "rubricBlock"
		}
		for (int i = 0; i < itemRefArray.length; i++)
			// Escribe cada una de las referencias a los �tems
			itemRefArray[i].creaAssessmentItemRef(xsw);
		xsw.writeEndElement();							// Cierra "assessmentSection"
	}
}