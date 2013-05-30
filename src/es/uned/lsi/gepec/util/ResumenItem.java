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
 * Clase que representa un resumen de los ítems (archivos XML independientes que representan al 
 * ítem). El resumen del ítem contiene el nombre del ítem (nombre del archivo XML) en un <code>
 * String</code>, el título del ítem en otro <code>String</code>, y el objeto <code>File</code> 
 * que representa al archivo XML del ítem. Contiene un constructor que inicializa todos sus 
 * valores, métodos "getters" para obtenerlos, e implementa la interfaz <code>Comparable</code> 
 * para permitir que se ordene una colección de objetos <code>ResumenItem</code>.
 * 
 * @author 	David Domínguez
 * @see		Comparable
 */
public class ResumenItem implements Comparable {

	/**
	 * Nombre del ítem (nombre del archivo XML).
	 */
	private String nombre;
	/**
	 * Título del ítem.
	 */
	private String titulo;
	/**
	 * File al archivo XML del ítem.
	 */
	private File archivo;

	/**
	 * Constructor de la clase que simplemente da a las variables de clase los valores 
	 * recibidos como parámetros.
	 * 
	 * @param 	nombre		string con el nombre del ítem
	 * @param 	titulo		string con el título del ítem
	 * @param 	archivo		file representando al archivo XML del ítem
	 */
	public ResumenItem(String nombre, String titulo, File archivo) {
		super();
		this.nombre = nombre;
		this.titulo = titulo;
		this.archivo = archivo;
	}

	/**
	 * Método "get" que devuelve el <code>String</code> con el nombre del ítem.
	 * 
	 * @return			string con el nombre del ítem
	 */
	public String getNombre() {
		return nombre;
	}

	/**
	 * Método "get" que devuelve el <code>String</code> con el título del ítem.
	 * 
	 * @return			string con el título del ítem
	 */
	public String getTitulo() {
		return titulo;
	}

	/**
	 * Método "get" que devuelve el objeto <code>File</code> que representa al archivo XML del 
	 * ítem.
	 * 
	 * @return			file con el archivo XML del ítem
	 */
	public File getArchivo() {
		return archivo;
	}

	/**
	 * Método definido por la interfaz <code>Comparable</code> para permitir la comparación 
	 * entre los objetos que implementen la interfaz. Este método utiliza el método del mismo 
	 * nombre implementado por la clase <code>String</code> para comparar los objetos <code>
	 * ResumenItem</code> según sus nombres. De este modo se permite ordenar de forma correcta 
	 * colecciones de objetos <code>ResumenItem</code> según el nombre del ítem. Para su 
	 * correcto funcionamiento, el método comprueba primero que el objeto con el que se compara 
	 * sea también de tipo <code>ResumenItem</code>.
	 * 
	 * @param 	o					objeto con el que comparar este ResumenItem
	 * @return						entero resultado de la comparación de los dos nombres de 
	 * 								los resúmenes de los ítems
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