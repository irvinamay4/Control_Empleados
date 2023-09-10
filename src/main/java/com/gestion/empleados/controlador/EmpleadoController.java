package com.gestion.empleados.controlador;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestion.empleados.entidades.Empleado;
import com.gestion.empleados.servicios.EmpleadoService;
import com.gestion.empleados.util.paginacion.PageRender;
import com.gestion.empleados.util.reportes.EmpleadoExporterExcel;
import com.gestion.empleados.util.reportes.EmpleadoExporterPDF;
import com.lowagie.text.DocumentException;

@Controller
public class EmpleadoController {
	
	@Autowired
	private EmpleadoService empleadoService;
	
	@GetMapping("/ver/{id}")
	public String verDetallesDelEmpleado(@PathVariable(value = "id") Long id, Map<String, Object> modelo,RedirectAttributes flash) {
		Empleado empleado = empleadoService.findOne(id);
		if (empleado ==null) {
			flash.addAttribute("error", "El empleado no existe en la db");
			return "redirect:/listar";
		}
		
		modelo.put("empleado", empleado);
		modelo.put("titulo", "Detalles del empleado " + empleado.getNombre());
		return "ver";
	}
	
	@GetMapping({"/","/listar",""})
	public String listarEmpleados(@RequestParam(name = "page", defaultValue = "0") int page, Model modelo) {
		//Elementos que mostrara en cada pagina
		Pageable pageRequest = PageRequest.of(page, 5);
		Page<Empleado> empleados = empleadoService.findAll(pageRequest);
		PageRender<Empleado> pageRender = new PageRender<>("/listar", empleados);
		modelo.addAttribute("titulo", "Listado de empleados");
		modelo.addAttribute("empleados", empleados);
		modelo.addAttribute("page", pageRender);
		
		return "listar";
		
	}
	
	@GetMapping("/form")
	public String mostrarFormularioDeRegistrarEmpleado(Map<String, Object> modelo) {
		//Para tener en el form los datos que necesita un empleado para registrarse
		Empleado empleado = new Empleado();
		modelo.put("empleado", empleado);
		modelo.put("titulo", "Registro de empleados");
		return "form";
	}
	
	@PostMapping("/form")
	public String guardarEmpleado(@Valid Empleado empleado, BindingResult result,Model modelo, RedirectAttributes flash, SessionStatus status) { //BindingResult obtiene todos los resultados cuando se validan los atributos
		if(result.hasErrors()) {
			modelo.addAttribute("titulo", "Registro de Empleado");
			return "form";
		}
		String id = String.valueOf(empleado.getId());
		String mensaje = (id != null) ? "El empleado ha sido editado con exito" : "Empleado registrado con exito";
		
		empleadoService.save(empleado);
		status.setComplete();
		
		flash.addFlashAttribute("success", mensaje);
		
		return "redirect:/listar";
		
	}
	
	@GetMapping("/form/{id}")
	public String editarEmpleado(@PathVariable(value = "id") Long id, Map<String, Object> modelo, RedirectAttributes flash) { //BindingResult obtiene todos los resultados cuando se validan los atributos
		Empleado empleado = null;
		
		if (id > 0) {
			empleado = empleadoService.findOne(id);
			if (empleado == null) {
				flash.addFlashAttribute("error", "El ID del empleado no existe en la base de datos");
				return "redirect:/listar";
				
			}
		}
		else {
			flash.addFlashAttribute("error", "El ID del empleado no puede ser 0");
			return "redirect:/listar";
		}
		modelo.put("empleado", empleado);
		modelo.put("titulo", "Edicion de empleado");
		return "form";		
	}
	
	@GetMapping("/eliminar/{id}")
	public String eliminarCliente(@PathVariable(value = "id") Long id, RedirectAttributes flash) {
		if (id > 0) {
			empleadoService.delete(id);
			flash.addAttribute("success", "Cliente eliminado con exito");
		}
		return "redirect:/listar";
	}
	
	@GetMapping("/exportarPDF")
	public void exportarListadoDeEmpleadosEnPDF(HttpServletResponse response) throws DocumentException, IOException {
		//Indicar tipo de contenido que devolvera
		response.setContentType("application/pdf");
		
		//Indicar formato de fecha
		DateFormat dateFormater = new SimpleDateFormat("yyyy-mm-dd_HH:mm:ss");
		String fechaActual = dateFormater.format(new Date());
				
		String cabecera = "Content-Disposition"; //Junto con attatchment permite descargar el archivo
		String valor = "attachment; filename=Empleados_" + fechaActual + ".pdf"; //Nombre sera ennviado con la fecha de hoy + .pdf
		
		//Lo ingresamos a la cabecera
		response.setHeader(cabecera, valor);
		
		List<Empleado> empleados = empleadoService.finAll();
		
		EmpleadoExporterPDF exporter = new EmpleadoExporterPDF(empleados);
		exporter.exportar(response);
		
	}
	
	@GetMapping("/exportarExcel")
	public void exportarListadoDeEmpleadosEnExcel(HttpServletResponse response) throws DocumentException, IOException {
		//Indicar tipo de contenido que devolvera
		response.setContentType("application/octec-stream"); //octec-stream para que responda en dat de tipo excel
		
		//Indicar formato de fecha
		DateFormat dateFormater = new SimpleDateFormat("yyyy-mm-dd_HH:mm:ss");
		String fechaActual = dateFormater.format(new Date());
				
		String cabecera = "Content-Disposition"; //Junto con attatchment permite descargar el archivo
		String valor = "attachment; filename=Empleados_" + fechaActual + ".xlsx"; //Nombre sera ennviado con la fecha de hoy + .pdf
		
		//Lo ingresamos a la cabecera
		response.setHeader(cabecera, valor);
		
		List<Empleado> empleados = empleadoService.finAll();
		
		EmpleadoExporterExcel exporter = new EmpleadoExporterExcel(empleados);
		exporter.exportar(response);
		
	}
}
