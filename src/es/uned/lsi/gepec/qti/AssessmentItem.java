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

import es.uned.lsi.gepec.util.MensajeEstado
;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Representación de un ítem XML de la norma QTI del IMS general. Es una clase abstracta, 
 * superclase de todas las clases contenedoras de los ítems XML. Esta clase representa un ítem 
 * XML general, y como tal, contiene los tres campos que tienen todos los ítems: <code>
 * identificador</code>, <code>título</code>, e <code>instrucciones</code> del ítem. Contiene 
 * un constructor que inicializa esos tres campos. También un método abstracto para escribir el 
 * ítem XML en disco y un método para escribir la cabecera común de todos los ítems y otro para 
 * el pie.
 *  
 * @author Blas Medina Alcudia
 *
 */
public abstract class AssessmentItem {

	
	/**
	 * El identificador del ítem, que será también el nombre del archivo.
	 */
	protected String identificador;
	/**
	 * Título del ítem.
	 */
	protected String titulo;
	/**
	 * Instrucciones para responder el ítem.
	 */
	protected String instrucciones;
	
	/**
	 * Constructor que le da valor a las tres variables de clase. Simplemente las inicializa a 
	 * esos valores.
	 * 
	 * @param 	identificador		identificador del ítem (y el nombre del archivo)
	 * @param 	titulo				título del ítem
	 * @param 	instrucciones		instrucciones para el ítem
	 */
	public AssessmentItem (String identificador, String titulo, String instrucciones) {
		
		this.identificador = identificador;
		this.titulo = titulo;
		this.instrucciones = instrucciones;
	}

	/**
	 * Método "get" para obtener el identificador actual de ítem. Para comprobar si el nombre 
	 * del fichero ya existe antes de intentar crearlo.
	 * 
	 * @return					string con el identificador actual del ítem
	 * @see		#setIdentificador
	 */
	public String getIdentificador() {
		return identificador;
	}

	/**
	 * Método "set" para establecer el identificador del ítem. Si el que tiene no es un nombre 
	 * de archivo válido con este método se le pone un nombre nuevo.
	 * 
	 * @param 	identificador	string con el nuevo identificador
	 * @see		#getIdentificador
	 */
	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	/**
	 * Escribe en disco el ítem XML al que representa esta clase. Debe ser sobreescrito por 
	 * todos los ítems distintos.El método se encarga de construir un ítem XML de acuerdo con 
	 * la norma QTI con los campos que tiene almacenados y en la dirección que recibe como 
	 * parámetro. Devuelve un <code>MensajeEstado</code> informando del resultado del proceso.
	 * 
	 * @param 	path		string con la dirección absoluta en la que crear el archivo XML	
	 * @param	sc			servletContext para acceder a los parámetros iniciales de "web.xml"
	 * @param	req			string con la url base de la Aplicación Web requerida
	 * @return				un mensajeEstado informando del estado final del proceso
	 * @throws 	IOException	si hay algún problema al intentar escribir el archivo en disco
	 */
	public abstract MensajeEstado creaItemXML(String path, String req) 
			throws IOException;
	
	/**
	 * Escribe en un <code>writer</code> específico para escribir archivos XML la cabecera 
	 * común a todos los tipos de ítem XML.
	 * 
	 * @param 	xsw					XMLStreamWriter, un writer específico para escribir 
	 * 								contenido XML
	 * @param	sc					servletContext para leer los parámetros del nombre de la 
	 * 								herramienta y la versión de "web.xml"
	 * @throws 	XMLStreamException	si hay algún problema con los datos XML a escribir
	 * @see		XMLStreamWriter
	 */
	protected void writeCabecera(XMLStreamWriter xsw) 
			throws XMLStreamException{
		xsw.writeStartDocument("ISO-8859-1","1.0");
		// Elemento raíz de todos los ítems XML
		xsw.writeStartElement("assessmentItem");
		xsw.writeDefaultNamespace("http://www.imsglobal.org/xsd/imsqti_v2p1");
		xsw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xsw.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xsw.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", 
				"http://www.imsglobal.org/xsd/imsqti_v2p1 imsqti_v2p1.xsd");
		// La implementación no admite ítems adaptativos
		xsw.writeAttribute("adaptive", "false");
		// La implementación no admite ítems dependientes del tiempo
		xsw.writeAttribute("timeDependent", "false");
		// Nombre y versión de esta herramienta de creación de ítems XML
		xsw.writeAttribute("toolName","Herramienta de Creación de Examen QTI. UNED");
		xsw.writeAttribute("toolVersion","1.0");
	}
	
	/**
	 * Escribe en un <code>writer</code> específico para escribir archivos XML el pie común a 
	 * todos los tipos de ítem XML. Al finalizar llama a <code>flush</code> del <code>writer
	 * </code> y finalmente lo cierra llamando a <code>close</code>.
	 * 
	 * @param 	xsw					XMLStreamWriter, un writer específico para escribir 
	 * 								contenido XML
	 * @param	sc					servletContext para leer la uri de la plantilla para el 
	 * 								procesado de la respuesta
	 * @param	req					strng con la URL base del proyecto
	 * @throws 	XMLStreamException	si hay algún problema con los datos XML a escribir
	 * @see		XMLStreamWriter
	 */
	protected void writePie(XMLStreamWriter xsw, String req) 
			throws XMLStreamException {
		xsw.writeEndElement();							// Cierra "itemBody"
		xsw.writeStartElement("responseProcessing");	// ResponseProcessing de la respuesta
		String template;
		
		// uri de la plantilla de respuesta a partir de la URL base del proyecto más su uri 
		// relativa
		template = req;
		xsw.writeAttribute("template", template);
		
		xsw.writeEndElement();							// Cierra responseProcessing
		xsw.writeEndElement();							// Cierra "assessmentItem"
		xsw.writeEndDocument();							// Fin del documento
		xsw.flush();									// Vacía el Writer
		xsw.close();									// Cierra el Writer
	}
}