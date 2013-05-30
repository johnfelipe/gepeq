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
 * Clase que contiene m�todos est�ticos que devuelven determinadas listas del contenido de los 
 * directorios.
 *  
 * @author David Dom�nguez
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
	 * Lista las im�genes incluidas en <code>directorio</code> (no en subdirectorios de �ste). 
	 * S�lo las im�genes permitidas, cuyas extensiones (y su versi�n en may�sculas), debe estar 
	 * incluida en el array de EXTENSIONES. Devuelve los nombres en un Vector.
	 *  
	 * @param 	directorio		file del directorio cuyas im�genes hay que listar
	 * @return					vector de <code>String</code>s con los nombres de las im�genes
	 * @see		File
	 * @see		java.util.Vector
	 * @see		java.util.Collection
	 * @see		java.util.Collections
	 * @see		org.apache.commons.io.FileUtils#listFiles(java.io.File, java.lang.String[], boolean)
	 */
	public static Vector ListaImagenes(File directorio){
		
		// Extensiones de im�genes permitidas
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

		// Vector con los nombres de las im�genes
		Vector /* String */ imagNames = new Vector();
		
		// Comprueba que el directorio existe
		if (directorio.exists()){
			// Comprueba que sea un directorio de verdad
			if (directorio.isDirectory()){
				// Busca las im�genes s�lo en el directorio actual (no subdirectorios)
				Collection c = FileUtils.listFiles(directorio, EXTENSIONES, false);
				Vector imagenes = new Vector(c);
				Collections.sort(imagenes);
				// Mete en un Vector los nombres de las im�genes y lo almacena en request
				for (int i = 0; i < imagenes.size(); i++)
					imagNames.add(((File)imagenes.elementAt(i)).getName());
			}
		}
		
		return imagNames;
	}
	
	/**
	 * Lista los �tems (en realidad archivos con extensi�n "xml") incluidos en <code>directorio
	 * </code> (no en subdirectorios de �ste). Devuelve un <code>Vector</code> con los nombres 
	 * de los archivos de �tems XML, su t�tulo y un objeto <code>File</code> apuntando a cada 
	 * �tem.
	 * 
	 * @param 	directorio	file del directorio a comprobar
	 * @return				vector de objetos <code>ResumenItem</code> con los nombres y los 
	 * 						t�tulos de los archivos de los �tems xml
	 * @throws 	IOException si hay alg�n problema leyendo el contenido de los archivos XML
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
			// Busca los �tems s�lo en el directorio actual (no subdirectorios)
			File[] files = directorio.listFiles();
			for (int i = 0; i < files.length; i++){
				// Comprueba que los archivos sean archivos XML
				if (files[i].isFile()){
					String nombre = files[i].getName();		// Nombre del archivo
					String extension = nombre.substring(nombre.lastIndexOf('.'),
							nombre.length());				// Extensi�n del archivo
					if (extension.equalsIgnoreCase(".xml")){
						// Si es un archivo XML se procesa usando StAX
						FileReader fr = null;
						try {
							fr = new FileReader(files[i]);
							XMLInputFactory xmlif = XMLInputFactory.newInstance();
							// Parser para el archivo XML
							XMLStreamReader xmlsr = xmlif.createXMLStreamReader(fr);
							// Boolean indicando si se ha encontrado el t�tulo del �tem
							boolean encontrado = false;
							// Busca el t�tulo en el archivo XML
							while(xmlsr.hasNext() && encontrado == false){
								// Si el cursor est� en una etiqueta de apertura
								if(xmlsr.getEventType() == XMLStreamReader.START_ELEMENT){
									// Si la etiqueta es la de apertura de "assesmentItem", en
									// la que se encuentra el atributo buscado, t�tulo
									if(xmlsr.getLocalName().equalsIgnoreCase("assessmentItem")){
										// Recorre todos los atributos buscando el t�tulo
										for (int j = 0; j < xmlsr.getAttributeCount() && 
												encontrado == false; j++) {
											// Lee el nombre del atributo
											String localName = xmlsr.getAttributeLocalName(j);
											// Si el atributo es el buscado
											if (localName.equals("title")) {
												// Resumen del �tem con el identificador, el 
												// t�tulo y el File represent�ndolo
												ResumenItem ri = new ResumenItem(nombre, 
														xmlsr.getAttributeValue(j), files[i]);
												// Se a�ade el resumen del �tem al vector
												items.add(ri);
												encontrado = true; // T�tulo del �tem encontrado
											}
										}
									}
								}
								xmlsr.next();		// Avanza al siguiente evento en el �tem XML
							}
							xmlsr.close();	// Cierra el parser una vez ha terminado con el �tem
						} catch (FileNotFoundException e) {
							// Si no se ha podido crear el reader para este �tem pasa al 
							// siguiente archivo sin hacer nada
						} catch (XMLStreamException e){
							// Si hay alg�n problema leyendo el archivo XML no se hace nada
							// y se pasa al siguiente archivo
						}
						fr.close();	 // Cierra el Reader del �tem actual para pasar al siguiente
					}
				}
			}
			Collections.sort(items);	// Ordena los res�menes de los �tems del Vector
		}
		
		return items;
	}
}