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
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;

import es.uned.lsi.gepec.util.*;

/**
 * Clase contenedora del �tem tipo "Hot Text". Contiene un campo para cada par�metro del �tem 
 * necesario para construir el archivo del �tem XML. Con los m�todos que incluye permite crear 
 * un �tem tipo "Hot Text" completo y crear el archivo XML en disco.
 * 
 * @author 	Blas Medina Alcudia
 * @see		Identificador
 */
public class HottextXML extends AssessmentItem {

	/**
	 * Pregunta del �tem.
	 */
	protected String pregunta;
	/**
	 * Textos que rodean a los hot texts.
	 */
	protected String[] textos;
	/**
	 * Hot texts a incluir en el texto.
	 */
	protected String[] hottexts;
	/**
	 * Identificadores de los hottexts.
	 */
	protected Identificador[] identHottexts;
	/**
	 * M�ximo n�mero de hot texts seleccionables.
	 */
	protected int maxChoices;
	/**
	 * Array de boolean con los hottexts correctos.
	 */
	protected boolean[] correcto;
	/**
	 * Cardinalidad de la variable de respuesta.
	 */
	protected String cardinalidad;

	/**
	 * Constructor que permite inicializar el objeto con un primer grupo de par�metros. Son 
	 * los que se recogen en la primera p�gina de la toma de datos. Para los par�metros 
	 * comunes a todos los �tems se llama al constructor de la superclase.
	 * 
	 * @param 	identificador	identificador del �tem
	 * @param 	titulo			t�tulo del �tem
	 * @param 	instrucciones	instrucciones del �tem
	 * @param 	pregunta		pregunta del �tem
	 */
	public HottextXML(String identificador, String titulo, String instrucciones, 
			String pregunta) {
		super(identificador, titulo, instrucciones);
		this.pregunta = pregunta;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>boolean</code> que indica si el 
	 * hot text en esa posici�n es correcto o no.
	 * 
	 * @param 	correcto		array de boolean indicando hot text correcto
	 */
	public void setCorrecto(boolean[] correcto) {
		this.correcto = correcto;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>String</code> que contiene cada 
	 * uno de los hot texts a incluir en el texto.
	 * 
	 * @param 	hottexts 		array con los hot texts
	 */
	public void setHottexts(String[] hottexts) {
		this.hottexts = hottexts;
	}

	/**
	 * M�todo que inicializa el <code>array</code> de objetos <code>Identificador</code> que 
	 * contiene en cada posici�n el identificador del hot text en esa misma posici�n.
	 * 
	 * @param 	identHottexts	array con los identificadores de los hot texts
	 */
	public void setIdentHottexts(Identificador[] identHottexts) {
		this.identHottexts = identHottexts;
	}

	/**
	 * M�todo que inicializa el n�mero m�ximo de hot texts que se pueden seleccionar como 
	 * correctos. En funci�n de �ste, inicializa <code>cardinalidad</code> a un valor adecuado.
	 * 
	 * @param 	maxChoices		entero indicando el n�mero m�ximo de elecciones
	 */
	public void setMaxChoices(int maxChoices) {
		this.maxChoices = maxChoices;
		switch(maxChoices){
			case 1: cardinalidad = "single"; break;
			default: cardinalidad = "multiple"; break;
		}
	}

	/**
	 * M�todo que inicializa el <code>array</code> de <code>String</code> con los textos que 
	 * rodean a los huecos. El n�mero de textos es igual al n�mero de huecos m�s uno.
	 * 
	 * @param 	textos		array con los textos que rodean a los huecos
	 */
	public void setTextos(String[] textos) {
		this.textos = textos;
	}

	/**
	 * M�todo que construye el �tem XML tipo "Hot Text" con los campos que tiene almacenados 
	 * en la direcci�n que recibe como par�metro. Devuelve informaci�n de estado.
	 * 
	 * @param 	path		direcci�n absoluta en la que crear el archivo XML	
	 * @param	sc			servletContext para acceder a los par�metros iniciales de "web.xml"
	 * @param	req			string con la url base de la Aplicaci�n Web requerida
	 * @return				informaci�n del estado final del proceso
	 * @throws 	IOException	si hay alg�n problema al intentar escribir el archivo en disco
	 * @see		XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
	 * @see		XMLStreamWriter
	 * @see		StringWriter
	 */
	public MensajeEstado creaItemXML(String path, String req) 
			throws IOException {
		
		MensajeEstado msg = new MensajeEstado();		// Mensaje de salida del m�todo
		
		try {
			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xsw = null;				// Writer para escribir datos XML
			StringWriter sw = new StringWriter();	// Buffer en el que escribir
			xsw = xof.createXMLStreamWriter(sw);	// El writer XML escribe en el buffer
			// Escribe cabecera com�n a todos los tipos de pregunta
			writeCabecera(xsw);
			xsw.writeAttribute("identifier", identificador);
			xsw.writeAttribute("title", titulo);
			xsw.writeStartElement("responseDeclaration");	// Declara la variable de respuesta
			xsw.writeAttribute("identifier", "RESPONSE");
			xsw.writeAttribute("cardinality", cardinalidad);
			xsw.writeAttribute("baseType", "identifier");
			xsw.writeStartElement("correctResponse");		// Respuestas correctas
			for (int i = 0; i < correcto.length; i++){
				if (correcto[i]){
					xsw.writeStartElement("value");			// Valor correcto
					xsw.writeCharacters(identHottexts[i].getIdentificador());
					xsw.writeEndElement();					// Cierra "value"	
				}
			}
			xsw.writeEndElement();							// Cierra "correctResponse"
			xsw.writeEndElement();							// Cierra "responseDeclaration"
			xsw.writeStartElement("outcomeDeclaration");	// Variable de puntuaci�n
			xsw.writeAttribute("identifier", "SCORE");
			xsw.writeAttribute("cardinality", "single");
			xsw.writeAttribute("baseType", "integer");
			xsw.writeStartElement("defaultValue");			// Valor por defecto de la salida
			xsw.writeStartElement("value");					// Valor
			xsw.writeCharacters("0");
			xsw.writeEndElement();							// Cierra "value"
			xsw.writeEndElement();							// Cierra "defaultValue"
			xsw.writeEndElement();							// Cierra "outcomeDeclaration"
			xsw.writeStartElement("itemBody");				// Cuerpo del �tem
			// Las instrucciones se escriben directamente puesto que es xhtml, sin que el 
			// writer XML modifique los datos
			if (instrucciones != null)
				ParserNeutro.escribeInstruccionesXML(instrucciones, xsw);
			xsw.writeStartElement("hottextInteraction");	// Interacci�n tipo hottext
			xsw.writeAttribute("responseIdentifier","RESPONSE");
			xsw.writeAttribute("maxChoices",Integer.toString(maxChoices));
			if (pregunta != null && !pregunta.equals("")){
				xsw.writeStartElement("prompt");			// Escribe la pregunta si la hay
				xsw.writeCharacters(pregunta);
				xsw.writeEndElement();						// Cierra "prompt"
			}
			xsw.writeStartElement("p");						// Mete el texto en un p�rrafo
			for (int i = 0; i < identHottexts.length; i++){
				if (textos[i] != null && !textos[i].equals(""))			// Escribe texto 'i'
					// Los Textos se escriben mediante el m�todo de escribir textos XML para 
					// sustituir los caracteres de nueva l�nea por los equivalentes <br /> de 
					// XHTML
					ParserNeutro.escribeTextoXML(textos[i], xsw);
				xsw.writeStartElement("hottext");			// Hottext 'i'
				xsw.writeAttribute("identifier", identHottexts[i].getIdentificador());
				xsw.writeCharacters(hottexts[i]);
				xsw.writeEndElement();						// Cierra "hottext" 'i'
			}
			// Escribe el �ltimo texto
			if (textos[textos.length - 1] != null && !textos[textos.length - 1].equals(""))
				ParserNeutro.escribeTextoXML(textos[textos.length - 1], xsw);
			xsw.writeEndElement();							// Cierra "p"
			xsw.writeEndElement();							// Cierra "hottextInteraction"
			// Escribe el final del �tem XML y cierra el writer
			writePie(xsw, req);
			
			// Vac�a y cierra el Writer
			sw.flush();
			sw.close();
			// Escribe en un String el contenido del Writer
			String fich = sw.toString();
			// Fichero de salida
			File file = new File(path + identificador + ".xml");
			// Escribe en el fichero de salida
			FileUtils.writeStringToFile(file, fich, null);			
		} catch (XMLStreamException e) {
			// Error en el procesamiento de los datos XML
			msg.setMensaje("Error procesando el archivo XML");
			msg.setEstado(false);
		}
		if (msg.isEstado()){
			// Si todo ha ido bien se iforma del resultado
			msg.setMensaje("�tem creado con �xito");	
		}
		
		return msg;
	}
}