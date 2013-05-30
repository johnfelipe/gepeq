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

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.io.FileUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Clase que contiene métodos estáticos que devuelven determinadas listas del contenido de los 
 * directorios.
 *  
 * @author David Domínguez
 *
 */
public class ListaArchivos {

	/**
	 * Devuelve un vector listando los nombres de los directorios que se incluyen en <code>
	 * directorio</code>. Antes comprueba que sea realmente un directorio.
	 * 
	 * @param 	directorio		file del directorio a comprobar
	 * @return					vector con <code>String</code>s con los nombres de los 
	 * 							subdirectorios de <code>directorio</code>
	 * @see		java.io.File
	 * @see		java.util.Vector
	 * @see		java.util.Collections
	 */
	public static Vector ListaDirectorios(File directorio){

		// Vector con los nombres de los directorios
		Vector /* String */ dirNames = new Vector();

		// Comprueba que exista el directorio
		if (directorio.exists()){
			// Comprueba que sea un directorio de verdad
			if (directorio.isDirectory()){
				// Busca los directorios del directorio actual
				File[] files = directorio.listFiles();
				Vector directorios = new Vector();
				for (int i = 0; i < files.length; i++){
					if (files[i].isDirectory())
						directorios.add(files[i]);
				}
				// Ordena el vector de directorios
				Collections.sort(directorios);
				// Mete en el vector los nombres de los directorios y lo almacena en request
				for (int i = 0; i < directorios.size(); i++)
					dirNames.add(((File)directorios.elementAt(i)).getName());
			}
		}
		
		return dirNames;

	}
	
	/**
	 * Lista las imágenes incluidas en <code>directorio</code> (no en subdirectorios de éste). 
	 * Sólo las imágenes permitidas, cuyas extensiones (y su versión en mayúsculas), debe estar 
	 * incluida en el array de EXTENSIONES. Devuelve los nombres en un Vector.
	 *  
	 * @param 	directorio		file del directorio cuyas imágenes hay que listar
	 * @return					vector de <code>String</code>s con los nombres de las imágenes
	 * @see		File
	 * @see		java.util.Vector
	 * @see		java.util.Collection
	 * @see		java.util.Collections
	 * @see		org.apache.commons.io.FileUtils#listFiles(java.io.File, java.lang.String[], boolean)
	 */
	public static Vector ListaImagenes(File directorio){
		
		// Extensiones de imágenes permitidas
		String[] EXTENSIONES = new String[10];
		EXTENSIONES[0]= "jpg";
		EXTENSIONES[1]= "jpeg";
		EXTENSIONES[2]= "gif";
		EXTENSIONES[3]= "bmp";
		EXTENSIONES[4]= "png";
		EXTENSIONES[5]= "JPG";
		EXTENSIONES[6]= "JPEG";
		EXTENSIONES[7]= "GIF";
		EXTENSIONES[8]= "BMP";
		EXTENSIONES[9]= "PNG";

		// Vector con los nombres de las imágenes
		Vector /* String */ imagNames = new Vector();
		
		// Comprueba que el directorio existe
		if (directorio.exists()){
			// Comprueba que sea un directorio de verdad
			if (directorio.isDirectory()){
				// Busca las imágenes sólo en el directorio actual (no subdirectorios)
				Collection c = FileUtils.listFiles(directorio, EXTENSIONES, false);
				Vector imagenes = new Vector(c);
				Collections.sort(imagenes);
				// Mete en un Vector los nombres de las imágenes y lo almacena en request
				for (int i = 0; i < imagenes.size(); i++)
					imagNames.add(((File)imagenes.elementAt(i)).getName());
			}
		}
		
		return imagNames;
	}
	
	/**
	 * Lista los ítems (en realidad archivos con extensión "xml") incluidos en <code>directorio
	 * </code> (no en subdirectorios de éste). Devuelve un <code>Vector</code> con los nombres 
	 * de los archivos de ítems XML, su título y un objeto <code>File</code> apuntando a cada 
	 * ítem.
	 * 
	 * @param 	directorio	file del directorio a comprobar
	 * @return				vector de objetos <code>ResumenItem</code> con los nombres y los 
	 * 						títulos de los archivos de los ítems xml
	 * @throws 	IOException si hay algún problema leyendo el contenido de los archivos XML
	 * @see		java.io.File
	 * @see		java.util.Vector
	 * @see		java.util.Collections
	 * @see		utilidades.ResumenItem
	 */
	public static Vector ListaItems (File directorio) throws IOException{
		
		// Vector con los objetos ResumenItem
		Vector /* ResumenItem */ items = new Vector();
		
		// Comprueba que el directorio existe y que sea un directorio, no un archivo
		if (directorio.exists() && directorio.isDirectory()){
			// Busca los ítems sólo en el directorio actual (no subdirectorios)
			File[] files = directorio.listFiles();
			for (int i = 0; i < files.length; i++){
				// Comprueba que los archivos sean archivos XML
				if (files[i].isFile()){
					String nombre = files[i].getName();		// Nombre del archivo
					String extension = nombre.substring(nombre.lastIndexOf('.'),
							nombre.length());				// Extensión del archivo
					if (extension.equalsIgnoreCase(".xml")){
						// Si es un archivo XML se procesa usando StAX
						FileReader fr = null;
						try {
							fr = new FileReader(files[i]);
							XMLInputFactory xmlif = XMLInputFactory.newInstance();
							// Parser para el archivo XML
							XMLStreamReader xmlsr = xmlif.createXMLStreamReader(fr);
							// Boolean indicando si se ha encontrado el título del ítem
							boolean encontrado = false;
							// Busca el título en el archivo XML
							while(xmlsr.hasNext() && encontrado == false){
								// Si el cursor está en una etiqueta de apertura
								if(xmlsr.getEventType() == XMLStreamReader.START_ELEMENT){
									// Si la etiqueta es la de apertura de "assesmentItem", en
									// la que se encuentra el atributo buscado, título
									if(xmlsr.getLocalName().equalsIgnoreCase("assessmentItem")){
										// Recorre todos los atributos buscando el título
										for (int j = 0; j < xmlsr.getAttributeCount() && 
												encontrado == false; j++) {
											// Lee el nombre del atributo
											String localName = xmlsr.getAttributeLocalName(j);
											// Si el atributo es el buscado
											if (localName.equals("title")) {
												// Resumen del ítem con el identificador, el 
												// título y el File representándolo
												ResumenItem ri = new ResumenItem(nombre, 
														xmlsr.getAttributeValue(j), files[i]);
												// Se añade el resumen del ítem al vector
												items.add(ri);
												encontrado = true; // Título del ítem encontrado
											}
										}
									}
								}
								xmlsr.next();		// Avanza al siguiente evento en el ítem XML
							}
							xmlsr.close();	// Cierra el parser una vez ha terminado con el ítem
						} catch (FileNotFoundException e) {
							// Si no se ha podido crear el reader para este ítem pasa al 
							// siguiente archivo sin hacer nada
						} catch (XMLStreamException e){
							// Si hay algún problema leyendo el archivo XML no se hace nada
							// y se pasa al siguiente archivo
						}
						fr.close();	 // Cierra el Reader del ítem actual para pasar al siguiente
					}
				}
			}
			Collections.sort(items);	// Ordena los resúmenes de los ítems del Vector
		}
		
		return items;
	}
}