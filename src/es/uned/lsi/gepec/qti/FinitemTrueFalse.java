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
import java.util.List;
import java.util.Vector;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.qti.AssessmentItem;

import es.uned.lsi.gepec.util.*;

/**
 * Componente de control impl�cito asociado a la p�gina JSP "finitem.jsp". Es la l�gica 
 * asociada a esa p�gina JSP. Su m�todo <code>doLogic</code> se ejecutar� siempre que sea 
 * llamada la p�gina. Permite al usuario elegir el directorio dentro del de los �tems donde 
 * almacenar el �tem XML y lo intenta crear y almacenar en disco.
 *  
 * @author 	Blas Medina Alcudia
 * @see		control.Control
 * 
 */
public class FinitemTrueFalse {

	/**
	 * Ser� llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * p�gina requerida es "fin.jsp". Ya que es llamado desde un filtro, ser� ejecutado 
	 * siempre que llegue una petici�n de un cliente y antes de que la petici�n llegue al 
	 * Servlet (p�gina JSP compilada a Servlet) que sirve la respuesta al cliente. Trata de 
	 * crear el �tem XML almacenado en sesi�n. Saca el objeto, que debe ser subclase de <code>
	 * AssessmentItem</code>, de sesi�n y lo crear� en la carpeta que elija el usuario. Si ya 
	 * existe ese nombre de archivo entonces permite modificar el identificador del �tem e 
	 * introducir un nuevo nombre. Permite al usuario crear nuevos directorios y navegar por 
	 * los directorios. Puede recibir informaci�n de la p�gina de crear nuevos directorios. 
	 * Luego redirecciona la respuesta a "fin.jsp" que informa al usuario del �xito o fracaso 
	 * del proceso.
	 * 
	 * @throws	IOException			si hay alg�n problema accediendo a los directorios o 
	 * 								leyendo su contenido o al escribir el archivo en disco
	 * @see 	control.Control#doLogic(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
	 * @see		javax.servlet.http.HttpServletRequest
	 * @see		javax.servlet.http.HttpServletResponse
	 * @see 	java.io.File
	 * @see 	xml.items.AssessmentItem
	 * @see 	utilidades.MensajeEstado
	 * @see		utilidades.ListaArchivos
	 */
	public boolean doLogic(String ruta, String identificador, String titulo, String pregunta, Boolean correct) throws IOException {

		// Directorio base de trabajo (guardado en "web.xml")
		String dirTrabajo = ruta;
		// Directorio seleccionado a mostrar en este momento
		String dirItemActual = ruta;
		// Directorio inicial de los �tems (guardado en "web.xml")
		String dirInicial = ruta;
		// Directorio superior del actual (null si estamos en el inicial)
		String dirSuperior = null;
		// Aviso recibido de otra p�gina a mostrar en esta. Se utiliza tambi�n para los avisos 
		// de la propia p�gina. Se sobreescribe porque los avisos no se pueden dar a la vez.
		// Si se permite introducir un nuevo identificador
		boolean muestraNuevoIdent = false;
		

				// Saca de sesi�n el Assessment �tem creado para crear el archivo XML del �tem
				AssessmentItem aItem = new TrueFalseXML(identificador,titulo,"",pregunta,true,correct);
				//String nombre = "blas";	// Nombre del archivo XML a crear

				String nombre = aItem.getIdentificador();	// Nombre del archivo XML a crear
				
				// Estado de la comprobaci�n del identificador del �tem
				boolean correcto = true;
				
				// Si se ha introducido un nuevo identificador crea el archivo con ese nombre, 
				// comprobando antes que sea un nombre correcto
				if (identificador != null && !identificador.equals("")){

					// Caracteres reservados no permitidos
					final String reservados = ";/?:@&=+$,\\*\"<>|";

					// Comprueba que los caracteres del nuevo identificador est�n permitidos
			        for(int i = 0; i < identificador.length() && correcto == true; i++) {
			        	char c = identificador.charAt(i);	// Lee el siguiente car�cter
			        	int marca = reservados.indexOf(c);
			        	if (marca >= 0 ){
							correcto = false;
							muestraNuevoIdent = true;// Permitir introducir en un nuevo nombre
							// Guarda en request si se muestra el campo de introducir un nuevo 
							// identificador
			        	}
			        }
			        if (correcto == true)
			        	nombre = identificador;
				}
				// Si el nombre del archivo es correcto se intenta crear el archivo del �tem
				if (correcto == true){
					File item = new File(ruta + "/" + 
							nombre + ".xml");
					if (item.exists()){			// Si ya existe un �tem con ese nombre
					} else {
						// Si no existe crea el �tem XML y va a fin.jsp para mostrar el
						// resultado
						//aItem.setIdentificador(nombre);
						MensajeEstado msg;
						//String url = "d:/item";
						// URL base del proyecto web
						String urlBase = "http://www.imsglobal.org/question/qti_v2p1/rptemplates/match_correct";
						msg = aItem.creaItemXML(ruta + 
								"/", urlBase);
					}
				}
				
		
		return true;
	}
}