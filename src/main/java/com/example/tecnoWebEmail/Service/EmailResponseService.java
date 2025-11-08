package com.example.tecnoWebEmail.Service;
import org.springframework.stereotype.Service;

import com.example.tecnoWebEmail.Models.Client;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailResponseService {
    private static final String SEPARATOR = "================================================";
    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera el encabezado estándar para todas las respuestas
     */
    public String generateHeader(String comando) {
        StringBuilder header = new StringBuilder();
        header.append(SEPARATOR).append("\n");
        header.append("    SISTEMA DE GESTIÓN - GRUPO 21SA\n");
        header.append("    Respuesta para comando: ").append(comando).append("\n");
        header.append("    Fecha: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        header.append(SEPARATOR).append("\n\n");
        return header.toString();
    }
    /**
     * Formatea la respuesta para listar clientss
     */

     public String formatErrorResponse(String error, String command) {
        StringBuilder response = new StringBuilder();
        response.append(generateHeader(command));
        
        response.append(" ERROR EN LA OPERACIÓN\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("Se produjo un error al procesar su solicitud:\n\n");
        response.append(" Detalle del error:\n");
        response.append("   ").append(error).append("\n\n");
        
        response.append(" AYUDA:\n");
        response.append("   • Verifique que todos los parámetros sean correctos\n");
        response.append("   • Asegúrese de usar el formato correcto del comando\n");
        response.append("   • Contacte al administrador si el problema persiste\n");

        return response.toString();
    }
    public String formatUnknownCommandResponse(String command) {
        StringBuilder response = new StringBuilder();
        response.append(generateHeader("COMANDO NO RECONOCIDO"));

        response.append(" COMANDO NO VÁLIDO\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("El comando '").append(command).append("' no es reconocido por el sistema.\n\n");

        response.append("========== LISTA DE COMANDOS DISPONIBLES ==========\n\n");

        response.append("--- GESTIÓN DE USUARIOS Y ROLES ---\n");
        response.append("   • LISUSU[]: Listar todos los usuarios.\n");
        response.append("   • BUSUSU[\"ci\"]: Buscar un usuario por su CI.\n");
        response.append("   • INSUSU[\"ci\",\"user\",\"pass\",\"email\",\"nom\",\"ape\",\"rolName\"]\n");
        response.append("   • UPDUSU[\"ci_buscar\",\"n_user\",\"n_email\",\"n_nom\",\"n_ape\",\"n_rolName\"]\n\n");
        response.append("   • LISROL[]: Listar todos los roles.\n");
        response.append("   • BUSROL[\"nombreRol\"]: Buscar un rol por su nombre.\n");
        response.append("   • INSROL[\"nombreRol\"]\n");
        response.append("   • UPDROL[\"rolActual\",\"rolNuevo\"]\n\n");

        response.append("--- GESTIÓN DE CLIENTES ---\n");
        response.append("   • LISCLI[]: Listar todos los clientes.\n");
        response.append("   • BUSCLI[\"ci\"]: Buscar un cliente por su CI.\n");
        response.append("   • INSCLI[\"ci\",\"nombre\",\"apellido\",\"email\",\"telefono\",\"direccion\"]\n\n");

        response.append("--- GESTIÓN DE PRODUCTOS E INSUMOS (Ejemplos) ---\n");
        response.append("   • LISPRO[]: Listar todos los productos.\n");
        response.append("   • INSPRO[\"sku\",\"nombre\",\"desc\",\"precioVenta\",\"stockInicial\"]\n");
        response.append("   • LISSUP[]: Listar todos los insumos.\n");
        response.append("   • INSSUP[\"nombre\",\"desc\",\"unidadMedida\",\"stockInicial\"]\n\n");

        response.append("--- FLUJO DE PEDIDOS Y PRODUCCIÓN (EN ORDEN) ---\n");
        response.append("   1. INSORD[\"ciCliente\",\"ciUsuario\",\"CondicionPago\"]\n");
        response.append("      (Ej: INSORD[\"123\",\"789\",\"Credit\"])\n");
        response.append("   2. ADDET[\"idOrden\",\"idProducto\",\"cantidad\"]\n");
        response.append("      (Ej: ADDET[\"1\",\"1\",\"5\"])\n");
        response.append("   3. CONFORD[\"idOrden\",\"numCuotas\"]\n");
        response.append("      (Ej Crédito: CONFORD[\"1\",\"3\"])\n");
        response.append("      (Ej Contado: CONFORD[\"1\",\"0\"])\n");
        response.append("   4. INSPROD[\"idDetalle\",\"fechaInicio\",\"fechaFin\"]\n");
        response.append("      (Ej: INSPROD[\"1\",\"2025-11-10\",\"2025-11-20\"])\n\n");

        response.append("--- CONSULTA DE PEDIDOS ---\n");
        response.append("   • LISORD[]: Listar todas las órdenes.\n");
        response.append("   • BUSORD[\"idOrden\"]: Buscar una orden por su ID.\n\n");

        response.append("--- GESTIÓN DE PAGOS Y CUOTAS ---\n");
        response.append("   5. INSPAG[\"idOrden\",\"monto\",\"tipoPago\",\"idCuota\"]\n");
        response.append("      (Ej Contado: INSPAG[\"1\",\"150.00\",\"Cash\",\"0\"])\n");
        response.append("      (Ej Crédito: INSPAG[\"1\",\"50.00\",\"Transfer\",\"1\"])\n");
        response.append("   • LISPAG[]: Listar todos los pagos.\n");
        response.append("   • BUSPAG[\"idPago\"]: Buscar un pago por su ID.\n");
        response.append("   • LISPEDPAG[\"idOrden\"]: Listar pagos de una orden.\n");
        response.append("   • LISCUPAG[\"idCuota\"]: Listar pagos de una cuota.\n\n");

        response.append("--- CONSULTA DE CUOTAS ---\n");
        response.append("   • LISCUP[\"idOrden\"]: Listar cuotas de una orden.\n");
        response.append("   • LISVEN[]: Listar todas las cuotas vencidas.\n");
        response.append("   • LISCES[\"estado\"]: Listar cuotas por estado (Pending, Partial, Paid).\n\n");

        response.append("NOTA: Todos los parámetros de texto deben ir entre comillas dobles.\n");
        response.append("Las fechas deben estar en formato YYYY-MM-DD.\n");

        return response.toString();
    }
}
