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
 * Clase que representa a una sección del Test según la norma QTI del IMS. Se utiliza para 
 * agrupar varios ítems dentro de un Test. Contiene varios atributos que se corresponden con 
 * los atributos definidos en la norma, como son el título de la sección, si es visible, si se 
 * sigue algún criterio de selección y ordenación, y el array de objetos <code>
 * AssessmentItemRef</code> con las referencias de los ítems incluidos en la sección. Tal y 
 * como define la norma esta clase hereda de la clase "SectionPart", por lo tanto también 
 * contendrá sus variables de clase.

 * @author Blas Medina Alcudia
 *
 */
public class AssessmentSection extends SectionPart {

	/**
	 * Título de la sección.
	 */
	private String titulo;
	/**
	 * Si la sección es visible para el candidato que realice el test.
	 */
	private boolean visible;
	/**
	 * Criterio de selección de los ítems posibles a seguir.
	 */
	private Selection seleccion;
	/**
	 * Criterio de ordenación de los ítems del test.
	 */
	private Ordering orden;
	/**
	 * Instrucciones del ítem. Es código XHTML.
	 */
	private String instrucciones;
	/**
	 * Array de referencias a los ítems que se incluyen en la sección.
	 */
	private AssessmentItemRef[] itemRefArray;
	
	/**
	 * Constructor que inicializa variables de clase. Inicializa las siguientes variables de 
	 * clase: <code>titulo</code>, <code>visible</code>, <code>seleccion</code>, <code>orden
	 * </code>, e <code>instrucciones</code>, y llamando al constructor de la superclase con el 
	 * identificador e iniciando ambos atributos de sección requerida y fija a falso, ya que 
	 * esta implementación de la norma QTI del IMS sólo permite una sección dentro del test, 
	 * por lo que esos parámetros no tendrán en cuenta.
	 * 
	 * @param 	identificador		identificador de la sección
	 * @param 	titulo				string con el título de la sección
	 * @param 	visible				boolean indicando la visibilidad de la sección
	 * @param 	seleccion			selection con el criterio de selección de los ítems
	 * @param 	orden				ordering con el criterio de ordenación de los ítems
	 * @param 	instrucciones		string con las instrucciones XHTML
	 */
	public AssessmentSection(Identificador identificador, String titulo, boolean visible, 
			Selection seleccion, Ordering orden, String instrucciones) {
		// Los parámetros requerido y fijo para la sección, como sólo hay una, no son 
		// necesarios y no se tienen en cuenta
		super(identificador, false, false);
		this.titulo = titulo;
		this.visible = visible;
		this.seleccion = seleccion;
		this.orden = orden;
		this.instrucciones = instrucciones;
	}

	/**
	 * Método "get" para obtener el array de objetos de referencia a los ítems, <code>
	 * AssessmentItemRef</code>, de la sección.
	 * 
	 * @return		array de objetos AssessmentItemRef de la sección
	 * @see 		#setItemRefArray
	 */
	public AssessmentItemRef[] getItemRefArray() {
		return itemRefArray;
	}

	/**
	 * Método "set" para establecer el array de objetos de referencia a los ítems,<code>
	 * AssessmentItemRef</code>, de la sección.
	 *  
	 * @param 	itemRefArray	array de objetos AssessmentItemRef de la sección
	 * @see #getItemRefArray
	 */
	public void setItemRefArray(AssessmentItemRef[] itemRefArray) {
		this.itemRefArray = itemRefArray;
	}

	/**
	 * Método "get" para obtener el criterio de ordenación de la sección.
	 * 
	 * @return		ordering con el criterio de ordenación
	 */
	public Ordering getOrden() {
		return orden;
	}

	/**
	 * Método "get" para obtener el criterio de selección de los ítems de la sección.
	 * 
	 * @return		selection con el criterio de selección
	 */
	public Selection getSeleccion() {
		return seleccion;
	}
	
	/**
	 * Calcula la URI relativa de todos los ítems de la sección. El método llama al método para 
	 * calcular la URI relativa de un ítem para cada uno de los objetos referencia de ítem de 
	 * la sección, <code>AssessmentItemRef</code>, pasándole el objeto <code>File</code> 
	 * representando la dirección en disco del Test.
	 * 
	 * @param 	referencia		file con la dirección final en disco del Test
	 */
	public void creaRefItems (File referencia){
		for (int i = 0; i < itemRefArray.length; i++)
			itemRefArray[i].creaItemRef(referencia);
	}
	
	/**
	 * Método que escribe este objeto <code>AssessmentSection</code> en el <code>XMLStreamWriter
	 * </code> recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaAssessmentSection (XMLStreamWriter xsw) throws XMLStreamException{
		
		xsw.writeStartElement("assessmentSection");		// Comienzo del elemento
		xsw.writeAttribute("identifier", identificador.getIdentificador());	// Identificador
		xsw.writeAttribute("title", titulo);			// Título de la sección
		xsw.writeAttribute("visible", Boolean.toString(visible));	// Atributo visible
		if (seleccion != null)							// Escribe la selección si la hay
			seleccion.creaSelection(xsw);
		if (orden != null)								// Escribe el ordenación si lo hay
			orden.creaOrdering(xsw);
		if (instrucciones != null) {					// Las instrucciones se escriben si hay
			xsw.writeStartElement("rubricBlock");		// Elemento para las instrucciones
			xsw.writeAttribute("view", "candidate");	// Las ve el candidato
			// Se escriben mediante el parser neutro ya que es código XHTML
			ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeEndElement();						// Cierra "rubricBlock"
		}
		for (int i = 0; i < itemRefArray.length; i++)
			// Escribe cada una de las referencias a los ítems
			itemRefArray[i].creaAssessmentItemRef(xsw);
		xsw.writeEndElement();							// Cierra "assessmentSection"
	}
}