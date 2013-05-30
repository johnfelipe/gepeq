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
 * Componente de control implícito asociado a la página JSP "finitem.jsp". Es la lógica 
 * asociada a esa página JSP. Su método <code>doLogic</code> se ejecutará siempre que sea 
 * llamada la página. Permite al usuario elegir el directorio dentro del de los ítems donde 
 * almacenar el ítem XML y lo intenta crear y almacenar en disco.
 *  
 * @author 	Blas Medina Alcudia
 * @see		control.Control
 * 
 */
public class FinitemTrueFalse {

	/**
	 * Será llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * página requerida es "fin.jsp". Ya que es llamado desde un filtro, será ejecutado 
	 * siempre que llegue una petición de un cliente y antes de que la petición llegue al 
	 * Servlet (página JSP compilada a Servlet) que sirve la respuesta al cliente. Trata de 
	 * crear el ítem XML almacenado en sesión. Saca el objeto, que debe ser subclase de <code>
	 * AssessmentItem</code>, de sesión y lo creará en la carpeta que elija el usuario. Si ya 
	 * existe ese nombre de archivo entonces permite modificar el identificador del ítem e 
	 * introducir un nuevo nombre. Permite al usuario crear nuevos directorios y navegar por 
	 * los directorios. Puede recibir información de la página de crear nuevos directorios. 
	 * Luego redirecciona la respuesta a "fin.jsp" que informa al usuario del éxito o fracaso 
	 * del proceso.
	 * 
	 * @throws	IOException			si hay algún problema accediendo a los directorios o 
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
		// Directorio inicial de los ítems (guardado en "web.xml")
		String dirInicial = ruta;
		// Directorio superior del actual (null si estamos en el inicial)
		String dirSuperior = null;
		// Aviso recibido de otra página a mostrar en esta. Se utiliza también para los avisos 
		// de la propia página. Se sobreescribe porque los avisos no se pueden dar a la vez.
		// Si se permite introducir un nuevo identificador
		boolean muestraNuevoIdent = false;
		

				// Saca de sesión el Assessment ítem creado para crear el archivo XML del ítem
				AssessmentItem aItem = new TrueFalseXML(identificador,titulo,"",pregunta,true,correct);
				//String nombre = "blas";	// Nombre del archivo XML a crear

				String nombre = aItem.getIdentificador();	// Nombre del archivo XML a crear
				
				// Estado de la comprobación del identificador del ítem
				boolean correcto = true;
				
				// Si se ha introducido un nuevo identificador crea el archivo con ese nombre, 
				// comprobando antes que sea un nombre correcto
				if (identificador != null && !identificador.equals("")){

					// Caracteres reservados no permitidos
					final String reservados = ";/?:@&=+$,\\*\"<>|";

					// Comprueba que los caracteres del nuevo identificador estén permitidos
			        for(int i = 0; i < identificador.length() && correcto == true; i++) {
			        	char c = identificador.charAt(i);	// Lee el siguiente carácter
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
				// Si el nombre del archivo es correcto se intenta crear el archivo del ítem
				if (correcto == true){
					File item = new File(ruta + "/" + 
							nombre + ".xml");
					if (item.exists()){			// Si ya existe un ítem con ese nombre
					} else {
						// Si no existe crea el ítem XML y va a fin.jsp para mostrar el
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