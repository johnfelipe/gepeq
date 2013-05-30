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

import java.io.IOException;

import es.uned.lsi.gepec.qti.*;

import es.uned.lsi.gepec.util.*;

/**
 * Componente de control implícito asociado a la página JSP "creartest.jsp". Es la lógica 
 * asociada a esa página JSP. Se ejecutará su método <code>doLogic</code> siempre que sea 
 * llamada la página. Su función es comprobar que todos los parámetros iniciales de la toma de 
 * datos para crear un nuevo test sean correctos, en cuyo caso redirecciona la respuesta 
 * a la página de seleccionar los ítems a incluir en el test. La clase implementa la interfaz 
 * <code>Control</code>, implementando su método <code>doLogic</code>, que es el único que 
 * contiene, y que es el encargado de realizar la lógica asociada a la página "creartest.jsp".
 * 
 * @author 	Blas Medina Alcudia
 * @see		control.Control
 * 
 */
public class Creartest  {

	/**
	 * Realiza la comprobación de los parámetros que se recogen en la página "creartest.jsp". 
	 * Será llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * página requerida es "creartest.jsp". Ya que es llamado desde un filtro, será ejecutado 
	 * siempre que llegue una petición de un cliente y antes de que la petición llegue al 
	 * Servlet (página JSP compilada a Servlet) que sirve la respuesta al cliente. Comprueba 
	 * uno por uno todos los parámetros, y, en caso de que sean correctos, crea un objeto <code>
	 * AssessmentTest</code> para guardar los datos asociados al test y lo guarda en el ámbito 
	 * de sesión para utilizarlos más adelante para terminar de crear el test.
	 * 
	 * @param	request				la petición http enviada por el cliente
	 * @param	response			la respuesta http a enviar desde el servidor
	 * @return						true para que se sigan ejecutando los demás filtros del 
	 * 								servidor y se envíe la respuesta correctamente
	 * @throws	IOException			no debe de producirse
	 * @throws	ServletException	si hay algún problema accediendo a request o response
	 * @see control.Control#doLogic(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @see 	control.ControlFilter
	 * @see		javax.servlet.http.HttpServletRequest
	 * @see		javax.servlet.http.HttpServletResponse
	 * @see		utilidades.EstadoWeb
	 * @see		utilidades.MensajeEstado
	 * @see		utilidades.EnteroPositivo
	 * @see		xml.test.AssessmentTest
	 */
	public boolean doLogic() throws IOException {
		
			// Caracteres reservados no permitidos
			final String reservados = ";/?:@&=+$,\\*\"<>|";

			String identificador = "Identif";	// Identificador del test 
			String titulo = "Tit";			// Título dele test
			String instrucciones = "Instruc";	// Instrucciones del test
			// Permitir revisión del test al finalizarlo
			String rev = "allowReview";
			String selec = "Select";	// Número de ítems a mostrar
			String Shuff = "Shuffle";		// Mezclar los ítems o no
			
			// Estado de la petición web. Almacena si son correctos los parámetros, los 
			// mensajes de aviso al usuario y dónde colocar el foco en la página
			EstadoWeb estado = new EstadoWeb();		
			
			boolean shuffle = false;		// Valor por defecto del parámetro
			if ( Shuff != null){			// Interpreta el valor marcado en la página 
				if (Shuff.equalsIgnoreCase("ON"))
					shuffle = true;
			}

			// Comprueba que estén introducidos todos los atributos obligatorios
			if (identificador != null && !identificador.equals("") &&
					titulo != null && !titulo.equals("") &&
					rev != null && !rev.equals("")){

				// Comprueba que los caracteres del identificador estén permitidos
		        for(int i = 0; i < identificador.length() && estado.isEstado() == true; i++) {
		        	char c = identificador.charAt(i);	// Lee el siguiente carácter
		        	int marca = reservados.indexOf(c);
		        	if (marca >= 0 ){
						estado.añadeError("Error: El carácter '" + reservados.charAt(marca) + 
								"' en la posición " + (i+1) + " del Identificador no está " +
								"permitido<br />", "Identif");
		        	}
		        }

		        // Permitir o no la revisión del documento
				boolean revision = Boolean.parseBoolean(rev);
				
				// Comprueba que el número de ítems a seleccionar esté vacío o sea un valor
				// válido
				EnteroPositivo seleccion;
				if (selec == null || selec.equals("")){
					seleccion = new EnteroPositivo("0");
					seleccion.esPositivo();			// Para actualizar el valor entero
				}
				else{
					seleccion = new EnteroPositivo(selec);
					MensajeEstado msg = seleccion.esPositivo();
					if(!msg.isEstado()){  // Si no es un número válido y mayor o igual que cero
						estado.añadeError("Error: el número de ítems a seleccionar " + 
								msg.getMensaje() + "<br />", "Select");
					}
				}
				
				// Comprueba que las instrucciones sean un código XHTML válido
				if (instrucciones != null && !instrucciones.equals("")){
					MensajeEstado msg = ParserNeutro.compruebaInstruccionesXHTML(instrucciones);
					if (msg.isEstado() == false)
						estado.añadeError(msg.getMensaje(), "Instruc");
				}

				// Si el estado es todo correcto pasa a generar el objeto contenedor del test 
				if ( estado.isEstado()){
					// Método de ordenación
					Ordering orden = null;
					if (shuffle == true)
						orden = new Ordering (shuffle);
					// Método de seleccionar ítems
					Selection select = null;
					// Si se selecciona algún número de ítems del total, es sin reemplazamiento, 
					// ya que los ítems son constantes, no contienen variables, y no tiene 
					// sentido repetirlos en una sección
					if (seleccion.getEnteroPositivo() != 0)
						select = new Selection(seleccion.getEnteroPositivo(), false);
					// Se inicializa el objeto Sección del test excepto las referencias a los
					// ítems, con un identificador válido que es válido, por eso no se comprueba
					AssessmentSection seccion = 
						new AssessmentSection(new Identificador("seccion1"), titulo, true, 
								select, orden, instrucciones);
					// Objeto de control para permitir la revisión o no
					ItemSessionControl control = new ItemSessionControl(revision);
					// Se inicializa el objeto Parte de test con la Sección anterior y con un 
					// identificador válido, que como sólo hay una parte, no se realiza ninguna 
					// comprobación, el objeto control anterior, y estableciendo la navegación y 
					// la presentación a esos valores que son los únicos que soporta esta 
					// implementación
					TestPart parte = new TestPart(new Identificador("parte1"), "nonlinear", 
							"simultaneous", control, seccion);
					// Objeto Test inicializado con los datos proporcionados por el usuario 
					// y los objetos creados más arriba
					AssessmentTest test = new AssessmentTest(identificador, titulo, parte);
					// Guarda el objeto AsessmentTest en sesión para seguir utilizándolo luego
				}
			}
			// Si no están todos los atributos obligatorios comprueba los que fallan
			// para informar al usuario
			else{
				if (identificador.equals(""))
					estado.añadeError("Debe introducir un Identificador<br />", "Identif");
				if (titulo.equals(""))
					estado.añadeError("Debe introducir un Título<br />", "Tit");
				if (rev == null || rev.equals(""))
					estado.añadeError("Debe marcar si desea permitir la revisión del test " +
							"al finalizarlo<br />", "Identif");
			}
			
		return true;
	}
}