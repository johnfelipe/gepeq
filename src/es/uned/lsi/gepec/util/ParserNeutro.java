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
package es.uned.lsi.gepec.util;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamConstants;

/**
 * Clase que contiene m�todos est�ticos para escribir con formato XML cadenas que ya tienen 
 * alg�n formato XHTML y comprobar que sean cadenas XML v�lidas.
 * 
 * @author 	David Dom�nguez
 */
public class ParserNeutro {

	/**
	 * M�todo que escribe un <code>String</code> con formato XHTML en un <code>XMLStreamWriter
	 * </code>. En primer lugar elimina las entidades "&amp;nbsp;" que se puedan encontrar en 
	 * las instrucciones, ya que no son soportadas por la norma, sustituy�ndolas por caracteres 
	 * de espacio en blanco normales. Luego crea un parser "StAX", <code>
	 * XMLStreamReader</code>, que va leyendo los eventos XML del <code>String</code> y 
	 * escribi�ndolos seg�n el evento que corresponda en el <code>XMLStreamWriter</code>.
	 * 
	 * @param 	xhtml				string con el texto XHTML a enviar al Writer XML
	 * @param 	xsw					xmlStreamWriter con el que escribir el c�digo XML 
	 * @throws	XMLStreamException	si hay alg�n error en la estructura del c�digo XML le�do o 
	 * 								al escribirlo
	 * @see		javax.xml.stream.XMLStreamReader
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public static void escribeInstruccionesXML (String xhtml, XMLStreamWriter xsw) 
			throws XMLStreamException{
		
		// Limpia la cadena eliminando las entidades de espacio en blanco no separable que 
		// pueden haberse introducido en el editor WYSIWYG
		final String entidad = "&nbsp;";
		final String sustitutoEntidad = " ";
		// Buffer provisional para hacer los cambios sobre la cadena
		StringBuffer buffer = new StringBuffer(xhtml);
		int indice = buffer.indexOf(entidad);
		// Se buscan las entidades "&nbsp;" para sustituirlas por espacios en blanco habituales
		while (indice != -1){
			buffer.replace(indice, indice + entidad.length(), sustitutoEntidad);
			xhtml = buffer.toString();
			indice = xhtml.indexOf(entidad, indice);
		}
		
		// La cadena XHTML hay que colocarla dentro de unas etiquetas de apertura y cierre 
		// iniciales ficticias que no son XHTML para poder pasarle el parser, ya que un 
		// documento XML le�do debe tener una etiqueta de principio y de fin que lo englobe por 
		// completo. Estas etiquetas se cuidar� m�s adelante de no se escribirlas en el 
		// XMLStreamWriter.
		final String Tag = "instrucciones";
		xhtml= "<" + Tag + ">" + xhtml + "</" + Tag + ">";
		// StringReader para que el parser lea la cadena XHTML recibida
		StringReader sr = new StringReader(xhtml);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		// Configura el comportamiento del parser
		xif.setProperty("javax.xml.stream.isReplacingEntityReferences", new Boolean(false));
		xif.setProperty("javax.xml.stream.isSupportingExternalEntities", new Boolean(false));
		// Parser para la cadena XHTML
		XMLStreamReader xsr = xif.createXMLStreamReader(sr);
		// Se recorren todos los eventos de la cadena XHTML utilizando el m�todo de escritura 
		// que corresponda al tipo de m�todo
		for(int event = xsr.next(); xsr.hasNext(); event = xsr.next()){
			switch (event){
				case XMLStreamConstants.START_ELEMENT:
					// Se escriben todas etiquetas de comienzo excepto la ficticia a�adida arriba
					if (!xsr.getLocalName().equals(Tag)){
						xsw.writeStartElement(xsr.getLocalName());
				        int numAtributos = xsr.getAttributeCount();
				        if (numAtributos > 0) {
				            for (int i = 0; i < numAtributos; i++){
				            	xsw.writeAttribute(xsr.getAttributeLocalName(i), 
				            			xsr.getAttributeValue(i));
				            }
				        }
					}
					break;
				case XMLStreamConstants.CHARACTERS:
					// Se escriben los caracteres
					xsw.writeCharacters(xsr.getText());
					break;
				case XMLStreamConstants.END_ELEMENT:
					// Se escriben todas las etiquetas de cierre excepto la ficticia a�adida
					// arriba
					if (!xsr.getLocalName().equals(Tag))
						xsw.writeEndElement();
					break;
				case XMLStreamConstants.SPACE:
					// Se escriben los espacios en blanco
					String space = xsr.getText();
					xsw.writeCharacters(space);
					break;
				case XMLStreamConstants.COMMENT:
					// Se escriben los comentarios
					xsw.writeComment(xsr.getText());
					break;
				case XMLStreamConstants.ENTITY_REFERENCE:
					// Se escribe la misma referencia a la entidad
					xsw.writeEntityRef(xsr.getLocalName());
			}
		}
		
		xsr.close();
		sr.close();
	}
	
	/**
	 * Escribe todos los caracteres con el m�todo <code>writeCharacters(String)</code>, excepto 
	 * los nueva l�nea, que los sustituye por el equivalente XHTML &lt;br /&gt; y los escribe 
	 * mediante el m�todo <code>writeEmptyElement</code> en el <code>XMLStreamWriter</code>.

	 * @param 	texto				cadena de texto plano a escribir sustituyendo los nueva l�nea
	 * 								por su car�cter equivalente &lt;br /&gt;
	 * @param 	xsw					xmlStreamWriter con el que escribir el c�digo XML 
	 * @throws	XMLStreamException	si hay alg�n error en la estructura del c�digo XML le�do o 
	 * 								al escribirlo
	 * @see		javax.xml.stream.XMLStreamReader
	 * @see		javax.xml.stream.XMLStreamWriter
	 */
	public static void escribeTextoXML(String texto, XMLStreamWriter xsw) 
			throws XMLStreamException {
		
		// Comienzo del fragmento a escribir en cada iterci�n
		int inicio = 0;
		// Fin del fragmento a escribir en cada iteraci�n
		int fin = texto.indexOf('\n');
		
		// Mientras haya caracteres nueva l�nea
		while (fin != -1){
			// El car�cter nueva l�nea puede ir precedido del retorno de carro o no. Se escribe 
			// el fragmento de texto sin los nueva l�nea ni los retorno de carro
			if (texto.charAt(fin - 1) == '\r'){
				if (inicio < fin - 1)
					xsw.writeCharacters(texto.substring(inicio, fin - 1));
			} else {
				if (inicio < fin)
					xsw.writeCharacters(texto.substring(inicio, fin));
			}
			
			// Despu�s de cada fragmento se escribe el c�digo nueva l�nea de XHTML
			xsw.writeEmptyElement("br");
			
			// Si el car�cter '\n' no es el �ltimo de la cadena de texto se inicializan a un 
			// nuevo valor las variables inicio y fin
			if (fin + 1 < texto.length()){
				inicio = fin + 1;
				fin = texto.indexOf('\n', inicio);
			} else{
				inicio = texto.length();
				fin = -1;
			}
						
		}
		
		// Comprueba que se hayan escrito la cadena completa por si queda un �ltimo fragmento 
		// por escribir
		if (inicio < texto.length())
			xsw.writeCharacters(texto.substring(inicio, texto.length()));
	}
	
	/**
	 * Comprueba un campo de instrucciones de un test o un �tem. Ya que es c�digo XHTML, se 
	 * comprueba mediante un parser XML, a la espera de alg�n error. Ya que es s�lo un fragmento 
	 * XHTML, antes de poder comprobar nada, se le pone una etiqueta de inicio ficticia y la de 
	 * fin, s�lo para la comprobaci�n.
	 * @param 	fragmento	string con la cadena XHTML a comprobar
	 * @return				mensajeEstado con el resultado final de la comprobaci�n
	 */
	public static MensajeEstado compruebaInstruccionesXHTML (String fragmento){
		MensajeEstado msg = new MensajeEstado();
		// La cadena XHTML hay que colocarla dentro de unas etiquetas de apertura y cierre 
		// iniciales ficticias que no son XHTML para poder pasarle el parser, ya que un 
		// documento XML le�do debe tener una etiqueta de principio y de fin que lo englobe por 
		// completo. Estas etiquetas se cuidar� m�s adelante de no se escribirlas en el 
		// XMLStreamWriter.
		final String Tag = "instrucciones";
		String fragmentoXML = "<" + Tag + ">" + fragmento + "</" + Tag + ">";
		// StringReader para que el parser lea la cadena XHTML recibida
		StringReader sr = new StringReader(fragmentoXML);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		// Configura el comportamiento del parser
		xif.setProperty("javax.xml.stream.isReplacingEntityReferences", new Boolean(false));
		xif.setProperty("javax.xml.stream.isSupportingExternalEntities", new Boolean(false));
		// Parser para la cadena XHTML
		XMLStreamReader xsr;
		try {
			xsr = xif.createXMLStreamReader(sr);
			// Se recorren todos los eventos de la cadena XHTML
			for(xsr.next(); xsr.hasNext(); xsr.next()){
				// Vac�o. No se hace nada con los datos le�dos. Simplemente se recorre la cadena 
				// XHTML para comprobar si es correcta.
			}
		} catch (XMLStreamException e) {
			msg.setEstado(false);
			msg.setMensaje("Las instrucciones tienen un c�digo incorrecto. Debe revisarlas." +
					"<br />");
		}
		
		return msg;
	}
}