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

/**
 * Clase que representa un resumen de los �tems (archivos XML independientes que representan al 
 * �tem). El resumen del �tem contiene el nombre del �tem (nombre del archivo XML) en un <code>
 * String</code>, el t�tulo del �tem en otro <code>String</code>, y el objeto <code>File</code> 
 * que representa al archivo XML del �tem. Contiene un constructor que inicializa todos sus 
 * valores, m�todos "getters" para obtenerlos, e implementa la interfaz <code>Comparable</code> 
 * para permitir que se ordene una colecci�n de objetos <code>ResumenItem</code>.
 * 
 * @author 	David Dom�nguez
 * @see		Comparable
 */
public class ResumenItem implements Comparable {

	/**
	 * Nombre del �tem (nombre del archivo XML).
	 */
	private String nombre;
	/**
	 * T�tulo del �tem.
	 */
	private String titulo;
	/**
	 * File al archivo XML del �tem.
	 */
	private File archivo;

	/**
	 * Constructor de la clase que simplemente da a las variables de clase los valores 
	 * recibidos como par�metros.
	 * 
	 * @param 	nombre		string con el nombre del �tem
	 * @param 	titulo		string con el t�tulo del �tem
	 * @param 	archivo		file representando al archivo XML del �tem
	 */
	public ResumenItem(String nombre, String titulo, File archivo) {
		super();
		this.nombre = nombre;
		this.titulo = titulo;
		this.archivo = archivo;
	}

	/**
	 * M�todo "get" que devuelve el <code>String</code> con el nombre del �tem.
	 * 
	 * @return			string con el nombre del �tem
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * M�todo "get" que devuelve el <code>String</code> con el t�tulo del �tem.
	 * 
	 * @return			string con el t�tulo del �tem
	 */
	public String getTitulo() {
		return titulo;
	}

	/**
	 * M�todo "get" que devuelve el objeto <code>File</code> que representa al archivo XML del 
	 * �tem.
	 * 
	 * @return			file con el archivo XML del �tem
	 */
	public File getArchivo() {
		return archivo;
	}

	/**
	 * M�todo definido por la interfaz <code>Comparable</code> para permitir la comparaci�n 
	 * entre los objetos que implementen la interfaz. Este m�todo utiliza el m�todo del mismo 
	 * nombre implementado por la clase <code>String</code> para comparar los objetos <code>
	 * ResumenItem</code> seg�n sus nombres. De este modo se permite ordenar de forma correcta 
	 * colecciones de objetos <code>ResumenItem</code> seg�n el nombre del �tem. Para su 
	 * correcto funcionamiento, el m�todo comprueba primero que el objeto con el que se compara 
	 * sea tambi�n de tipo <code>ResumenItem</code>.
	 * 
	 * @param 	o					objeto con el que comparar este ResumenItem
	 * @return						entero resultado de la comparaci�n de los dos nombres de 
	 * 								los res�menes de los �tems
	 * @throws	ClassCastException	si el objeto con el que comparar no es de este mismo tipo
	 */
	public int compareTo(Object o) {
		
		// Comprueba que el objeto "o" sea una instancia de esta misma clase
		if (!(o instanceof ResumenItem))
			throw new ClassCastException ("Se deben comparar dos objetos \"ResumenItem\"");
		else {
			ResumenItem ri = (ResumenItem) o;
			return nombre.compareTo(ri.nombre);
		}
	}
}