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
 * Clase que representa una Parte de un Test según la norma QTI del IMS. Se utiliza para 
 * agrupar Secciones. Contiene unos atributos que se corresponden con los definidos en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public class TestPart {

	/**
	 * Identificador de la Parte del Test.
	 */
	private Identificador identificador;
	/**
	 * Modo de navegación por la Parte del Test.
	 */
	private String navegacion;
	/**
	 * Modo de presentación de la Parte del Test.
	 */
	private String presentacion;
	/**
	 * Objeto de control de la Parte del Test.
	 */
	private ItemSessionControl control;
	/**
	 * Sección que incluye esta Parte del Test. Esta implementación sólo soporta una Sección por 
	 * Parte de Test.
	 */
	private AssessmentSection seccion;
	
	/**
	 * Constructor que simplemente inicializa las variables de la clase.
	 * 
	 * @param 	identificador	identificador con un identificador válido
	 * @param 	navegacion		string con el modo de navegación
	 * @param 	presentacion	string con el modo de presentación
	 * @param 	control			itemSessionControl con el control de la sesión
	 * @param 	seccion			assessmentSection con la sección que incluye
	 */
	public TestPart(Identificador identificador, String navegacion, String presentacion, 
			ItemSessionControl control, AssessmentSection seccion) {
		this.identificador = identificador;
		this.navegacion = navegacion;
		this.presentacion = presentacion;
		this.control = control;
		this.seccion = seccion;
	}
	
	/**
	 * Método "get" para obtener la Sección, el objeto <code>AssessmentSection</code> que 
	 * incluye esta Parte del Test.
	 * 
	 * @return					assessmentSection con la Sección
	 */
	public AssessmentSection getSeccion() {
		return seccion;
	}
	
	/**
	 * Método que escribe este objeto <code>TestPart</code> en el <code>XMLStreamWriter</code> 
	 * recibido como parámetro, según la norma QTI definida.
	 * 
	 * @param 	xsw						xmlStreamWriter con el que escribir el elemento 
	 * 									correspondiente a esta clase en el archivo XML
	 * @throws 	XMLStreamException		si hay algún problema al escribir con el <code>
	 * 									XMLStremaWriter</code> los datos XML en el archivo
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public void creaTestPart(XMLStreamWriter xsw) throws XMLStreamException{
		xsw.writeStartElement("testPart");					// Escribe el elemento
		xsw.writeAttribute("identifier", identificador.getIdentificador());	// Identificador
		xsw.writeAttribute("navigationMode", navegacion);	// Modo de navegación
		xsw.writeAttribute("submissionMode", presentacion);	// Modo de presentación
		control.creaItemSessionControl(xsw);				// Escribe el control de la sesión
		seccion.creaAssessmentSection(xsw);					// Escribe la sección
		xsw.writeEndElement();								// Cierra "testPart"
	}
}