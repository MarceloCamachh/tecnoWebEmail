package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Service.EmailResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelpCommand {

    @Autowired
    private EmailResponseService emailResponseService;

    public String handleHelp(String[] parameters) {
        try {
            StringBuilder help = new StringBuilder();
            help.append("=== SISTEMA DE GESTIÓN - GRUPO 21SA ===\n\n");
            help.append("Formato: COMANDO[\"parametro1\",\"parametro2\",...]\n");
            help.append("Nota: Use \"*\" para listar todos los elementos\n\n");
            
            // Comandos de Cliente
            help.append("CLIENTES:\n");
            help.append("• LISCLI[\"*\"] - Listar todos los clientes\n");
            help.append("• INSCLI[\"ci\",\"nombre\",\"correo\",\"telefono\",\"direccion\"] - Insertar cliente\n");
            help.append("• BUSCLI[\"ci\"] - Buscar cliente por CI\n\n");

            // Comandos de Roles
            help.append("ROLES:\n");
            help.append("• LISROL[\"*\"] - Listar todos los roles\n");
            help.append("• INSROL[\"nombre\",\"descripcion\"] - Insertar rol\n");
            help.append("• BUSROL[\"nombre\"] - Buscar rol por nombre\n");
            help.append("• UPDROL[\"id\",\"nombre\",\"descripcion\"] - Actualizar rol\n\n");

            // Comandos de Usuario
            help.append("USUARIOS:\n");
            help.append("• LISUSU[\"*\"] - Listar todos los usuarios\n");
            help.append("• BUSUSU[\"ci\"] - Buscar usuario por CI\n");
            help.append("• INSUSU[\"ci\",\"nombre\",\"correo\",\"telefono\",\"direccion\",\"password\",\"roleId\"] - Insertar usuario\n");
            help.append("• UPDUSU[\"id\",\"nombre\",\"correo\",\"telefono\",\"direccion\"] - Actualizar usuario\n\n");

            // Comandos de Producto
            help.append("PRODUCTOS:\n");
            help.append("• LISPRO[\"*\"] - Listar todos los productos\n");
            help.append("• BUSPRO[\"sku\"] - Buscar producto por SKU\n");
            help.append("• INSPRO[\"sku\",\"nombre\",\"descripcion\",\"precio\",\"stockInicial\"] - Insertar producto\n");
            help.append("• UPDPRO[\"sku\",\"nuevoNombre\",\"nuevaDescripcion\",\"nuevoPrecio\"] - Actualizar producto\n");
            help.append("• ENTPRO[\"productId\",\"cantidad\",\"motivo\"] - Registrar entrada de stock\n");
            help.append("• SALPRO[\"productId\",\"cantidad\",\"motivo\"] - Registrar salida de stock\n");
            help.append("• ADJPRO[\"productId\",\"nuevoStock\",\"motivo\"] - Ajustar stock\n\n");

            // Comandos de Insumo
            help.append("INSUMOS:\n");
            help.append("• LISSUP[\"*\"] - Listar todos los insumos\n");
            help.append("• BUSSUP[\"nombre\"] - Buscar insumo por nombre\n");
            help.append("• INSSUP[\"nombre\",\"descripcion\",\"unidadMedida\",\"stockInicial\"] - Insertar insumo\n");
            help.append("• UPDSUP[\"id\",\"nombre\",\"descripcion\",\"unidadMedida\"] - Actualizar insumo\n");
            help.append("• DELSUP[\"id\"] - Eliminar insumo\n");
            help.append("• ENTSUP[\"supplyId\",\"cantidad\",\"motivo\"] - Registrar entrada\n");
            help.append("• SALSUP[\"supplyId\",\"cantidad\",\"motivo\"] - Registrar salida\n");
            help.append("• ADJSUP[\"supplyId\",\"nuevoStock\",\"motivo\"] - Ajustar stock\n");
            help.append("• MOVSUP[\"supplyId\"] - Listar movimientos de insumo\n\n");

            // Comandos de Relación Producto-Insumo
            help.append("RELACIONES PRODUCTO-INSUMO:\n");
            help.append("• ADDSUPP[\"productId\",\"supplyId\",\"requiredAmount\"] - Agregar insumo a producto\n");
            help.append("• REMSUPP[\"productId\",\"supplyId\"] - Remover insumo de producto\n");
            help.append("• UPDPSP[\"productId\",\"supplyId\",\"newAmount\"] - Actualizar cantidad requerida\n");
            help.append("• LISPSP[\"productId\"] - Listar insumos de un producto\n");
            help.append("• LISUPP[\"supplyId\"] - Listar productos que usan un insumo\n");
            help.append("• CALRSU[\"productId\",\"quantity\"] - Calcular insumos requeridos\n");
            help.append("• VALSUPP[\"productId\",\"quantity\"] - Validar disponibilidad de insumos\n");
            help.append("• CONSUPP[\"productId\",\"quantity\",\"productionOrderId\"] - Consumir insumos para producción\n\n");

            // Comandos de Orden
            help.append("ÓRDENES:\n");
            help.append("• LISORD[\"*\"] - Listar todas las órdenes\n");
            help.append("• BUSORD[\"id\"] - Buscar orden por ID\n");
            help.append("• INSORD[\"idCliente\",\"descripcion\",\"monto\"] - Insertar orden\n");
            help.append("• CONFORD[\"id\"] - Confirmar orden\n\n");

            // Comandos de Detalle de Orden
            help.append("DETALLES DE ORDEN:\n");
            help.append("• ADDET[\"orderId\",\"productId\",\"cantidad\",\"precioUnitario\"] - Agregar detalle a orden\n\n");

            // Comandos de Orden de Producción
            help.append("ÓRDENES DE PRODUCCIÓN:\n");
            help.append("• INSPROD[\"productId\",\"quantity\",\"estimatedCompletionDate\"] - Crear orden de producción\n\n");

            // Comandos de Cuotas
            help.append("CUOTAS:\n");
            help.append("• LISCUP[\"orderId\"] - Listar cuotas de una orden\n");
            help.append("• LISVEN[\"*\"] - Listar cuotas vencidas no pagadas\n");
            help.append("• LISCES[\"estado\"] - Listar cuotas por estado\n\n");

            // Comandos de Pagos
            help.append("PAGOS:\n");
            help.append("• INSPAG[\"installmentId\",\"monto\",\"metodoPago\",\"comprobante\"] - Registrar pago\n");
            help.append("• LISPAG[\"*\"] - Listar todos los pagos\n");
            help.append("• BUSPAG[\"id\"] - Buscar pago por ID\n");
            help.append("• LISPEDPAG[\"orderId\"] - Listar pagos de una orden\n");
            help.append("• LISCUPAG[\"installmentId\"] - Listar pagos de una cuota\n\n");

            // Comando de ayuda
            help.append("AYUDA:\n");
            help.append("• HELP[\"*\"] - Mostrar esta ayuda\n\n");

            help.append("=== NOTAS IMPORTANTES ===\n");
            help.append("• Los parámetros deben ir entre comillas dobles y separados por comas\n");
            help.append("• Use * como parámetro para listar todos los elementos\n");
            help.append("• Las cantidades/stocks deben ser números enteros positivos\n");
            help.append("• Los IDs son números enteros\n");
            help.append("• Los montos y precios usan punto como separador decimal\n");
            help.append("• Las fechas deben estar en formato YYYY-MM-DD\n\n");
            help.append("Sistema desarrollado por TecnoWeb 2025 - Grupo 21SA");

            StringBuilder response = new StringBuilder();
            response.append(emailResponseService.generateHeader("HELP"));
            response.append(help.toString());
            return response.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al generar ayuda: " + e.getMessage(), "HELP");
        }
    }
}