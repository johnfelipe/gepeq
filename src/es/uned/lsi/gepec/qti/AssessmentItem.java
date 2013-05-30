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
 * Representaci�n de un �tem XML de la norma QTI del IMS general. Es una clase abstracta, 
 * superclase de todas las clases contenedoras de los �tems XML. Esta clase representa un �tem 
 * XML general, y como tal, contiene los tres campos que tienen todos los �tems: <code>
 * identificador</code>, <code>t�tulo</code>, e <code>instrucciones</code> del �tem. Contiene 
 * un constructor que inicializa esos tres campos. Tambi�n un m�todo abstracto para escribir el 
 * �tem XML en disco y un m�todo para escribir la cabecera com�n de todos los �tems y otro para 
 * el pie.
 *  
 * @author Blas Medina Alcudia
 *
 */
public abstract class AssessmentItem {

	
	/**
	 * El identificador del �tem, que ser� tambi�n el nombre del archivo.
	 */
	protected String identificador;
	/**
	 * T�tulo del �tem.
	 */
	protected String titulo;
	/**
	 * Instrucciones para responder el �tem.
	 */
	protected String instrucciones;
	
	/**
	 * Constructor que le da valor a las tres variables de clase. Simplemente las inicializa a 
	 * esos valores.
	 * 
	 * @param 	identificador		identificador del �tem (y el nombre del archivo)
	 * @param 	titulo				t�tulo del �tem
	 * @param 	instrucciones		instrucciones para el �tem
	 */
	public AssessmentItem (String identificador, String titulo, String instrucciones) {
		
		this.identificador = identificador;
		this.titulo = titulo;
		this.instrucciones = instrucciones;
	}

	/**
	 * M�todo "get" para obtener el identificador actual de �tem. Para comprobar si el nombre 
	 * del fichero ya existe antes de intentar crearlo.
	 * 
	 * @return					string con el identificador actual del �tem
	 * @see		#setIdentificador
	 */
	public String getIdentificador() {
		return identificador;
	}

	/**
	 * M�todo "set" para establecer el identificador del �tem. Si el que tiene no es un nombre 
	 * de archivo v�lido con este m�todo se le pone un nombre nuevo.
	 * 
	 * @param 	identificador	string con el nuevo identificador
	 * @see		#getIdentificador
	 */
	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	/**
	 * Escribe en disco el �tem XML al que representa esta clase. Debe ser sobreescrito por 
	 * todos los �tems distintos.El m�todo se encarga de construir un �tem XML de acuerdo con 
	 * la norma QTI con los campos que tiene almacenados y en la direcci�n que recibe como 
	 * par�metro. Devuelve un <code>MensajeEstado</code> informando del resultado del proceso.
	 * 
	 * @param 	path		string con la direcci�n absoluta en la que crear el archivo XML	
	 * @param	sc			servletContext para acceder a los par�metros iniciales de "web.xml"
	 * @param	req			string con la url base de la Aplicaci�n Web requerida
	 * @return				un mensajeEstado informando del estado final del proceso
	 * @throws 	IOException	si hay alg�n problema al intentar escribir el archivo en disco
	 */
	public abstract MensajeEstado creaItemXML(String path, String req) 
			throws IOException;
	
	/**
	 * Escribe en un <code>writer</code> espec�fico para escribir archivos XML la cabecera 
	 * com�n a todos los tipos de �tem XML.
	 * 
	 * @param 	xsw					XMLStreamWriter, un writer espec�fico para escribir 
	 * 								contenido XML
	 * @param	sc					servletContext para leer los par�metros del nombre de la 
	 * 								herramienta y la versi�n de "web.xml"
	 * @throws 	XMLStreamException	si hay alg�n problema con los datos XML a escribir
	 * @see		XMLStreamWriter
	 */
	protected void writeCabecera(XMLStreamWriter xsw) 
			throws XMLStreamException{
		xsw.writeStartDocument("ISO-8859-1","1.0");
		// Elemento ra�z de todos los �tems XML
		xsw.writeStartElement("assessmentItem");
		xsw.writeDefaultNamespace("http://www.imsglobal.org/xsd/imsqti_v2p1");
		xsw.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xsw.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		xsw.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", 
				"http://www.imsglobal.org/xsd/imsqti_v2p1 imsqti_v2p1.xsd");
		// La implementaci�n no admite �tems adaptativos
		xsw.writeAttribute("adaptive", "false");
		// La implementaci�n no admite �tems dependientes del tiempo
		xsw.writeAttribute("timeDependent", "false");
		// Nombre y versi�n de esta herramienta de creaci�n de �tems XML
		xsw.writeAttribute("toolName","Herramienta de Creaci�n de Examen QTI. UNED");
		xsw.writeAttribute("toolVersion","1.0");
	}
	
	/**
	 * Escribe en un <code>writer</code> espec�fico para escribir archivos XML el pie com�n a 
	 * todos los tipos de �tem XML. Al finalizar llama a <code>flush</code> del <code>writer
	 * </code> y finalmente lo cierra llamando a <code>close</code>.
	 * 
	 * @param 	xsw					XMLStreamWriter, un writer espec�fico para escribir 
	 * 								contenido XML
	 * @param	sc					servletContext para leer la uri de la plantilla para el 
	 * 								procesado de la respuesta
	 * @param	req					strng con la URL base del proyecto
	 * @throws 	XMLStreamException	si hay alg�n problema con los datos XML a escribir
	 * @see		XMLStreamWriter
	 */
	protected void writePie(XMLStreamWriter xsw, String req) 
			throws XMLStreamException {
		xsw.writeEndElement();							// Cierra "itemBody"
		xsw.writeStartElement("responseProcessing");	// ResponseProcessing de la respuesta
		String template;
		
		// uri de la plantilla de respuesta a partir de la URL base del proyecto m�s su uri 
		// relativa
		template = req;
		xsw.writeAttribute("template", template);
		
		xsw.writeEndElement();							// Cierra responseProcessing
		xsw.writeEndElement();							// Cierra "assessmentItem"
		xsw.writeEndDocument();							// Fin del documento
		xsw.flush();									// Vac�a el Writer
		xsw.close();									// Cierra el Writer
	}
}