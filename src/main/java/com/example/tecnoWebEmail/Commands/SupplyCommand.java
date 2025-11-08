package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Supply;
import com.example.tecnoWebEmail.Models.SupplyMovement;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.SupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SupplyCommand {

    @Autowired
    private SupplyService supplyService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    // LISSUP -> listar todos los insumos
    public String handleListSupplies() {
        try {
            List<Supply> supplies = supplyService.getAllSupplies();
            return formatListSuppliesResponse(supplies, "LISSUP");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar insumos: " + e.getMessage(), "LISSUP");
        }
    }

    // BUSSUP ["nombre"] -> buscar por nombre
    public String handleSearchSupplyByName(String[] parameters) {
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [nombre] faltante.", "BUSSUP");
            }
            String nombre = parameters[0].trim();
            Supply supply = supplyService.getSupplyByName(nombre);
            if (supply == null) {
                return emailResponseService.formatErrorResponse("Insumo no encontrado con nombre: " + nombre, "BUSSUP");
            }
            return formatSingleSupplyResponse(supply, "INSUMO ENCONTRADO", "BUSSUP");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al buscar insumo: " + e.getMessage(), "BUSSUP");
        }
    }

    // INSSUP ["nombre","descripcion","unidadMedida","stockInicial"]
    public String handleInsertSupply(String[] parameters) {
        try {
            if (parameters.length < 4) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 4: [nombre,descripcion,unidadMedida,stockInicial]", "INSSUP");
            }

            String nombre = parameters[0].trim();
            String descripcion = parameters[1].trim();
            String unidad = parameters[2].trim();
            BigDecimal stockInicial = new BigDecimal(parameters[3].trim());

            Supply s = new Supply();
            s.setNombre(nombre);
            s.setDescripcion(descripcion);
            s.setUnidadMedida(unidad);
            s.setStockActual(stockInicial);

            Supply created = supplyService.createSupply(s);
            return formatSingleSupplyResponse(created, "INSUMO CREADO EXITOSAMENTE", "INSSUP");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al crear insumo: " + e.getMessage(), "INSSUP");
        }
    }

    // UPDSUP ["id","nombre","descripcion","unidadMedida"]
    public String handleUpdateSupply(String[] parameters) {
        try {
            if (parameters.length < 4) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 4: [id,nombre,descripcion,unidadMedida]", "UPDSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            Supply existing = supplyService.getSupplyById(id)
                    .orElseThrow(() -> new RuntimeException("Insumo no encontrado con id: " + id));

            existing.setNombre(parameters[1].trim());
            existing.setDescripcion(parameters[2].trim());
            existing.setUnidadMedida(parameters[3].trim());

            Supply updated = supplyService.updateSupply(id, existing);
            return formatSingleSupplyResponse(updated, "INSUMO ACTUALIZADO", "UPDSUP");
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al actualizar insumo: " + e.getMessage(), "UPDSUP");
        }
    }

    // DELSUP ["id"] -> eliminar insumo
    public String handleDeleteSupply(String[] parameters) {
        try {
            if (parameters.length < 1) {
                return emailResponseService.formatErrorResponse("Parámetro [id] faltante.", "DELSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            supplyService.deleteSupply(id);
            return emailResponseService.generateHeader("DELSUP") + " Insumo eliminado.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al eliminar insumo: " + e.getMessage(), "DELSUP");
        }
    }

    // ENTSUP ["supplyId","cantidad","motivo"] -> entrada
    public String handleRegisterEntry(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [supplyId,cantidad,motivo]", "ENTSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            BigDecimal qty = new BigDecimal(parameters[1].trim());
            String reason = parameters[2].trim();

            supplyService.registerMovement(id, SupplyMovement.MovementType.ENTRY, qty, null, reason);
            return emailResponseService.generateHeader("ENTSUP") + " Entrada registrada.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al registrar entrada: " + e.getMessage(), "ENTSUP");
        }
    }

    // SALSUP ["supplyId","cantidad","motivo"] -> salida
    public String handleRegisterExit(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [supplyId,cantidad,motivo]", "SALSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            BigDecimal qty = new BigDecimal(parameters[1].trim());
            String reason = parameters[2].trim();

            supplyService.registerMovement(id, SupplyMovement.MovementType.EXIT, qty, null, reason);
            return emailResponseService.generateHeader("SALSUP") + " Salida registrada.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al registrar salida: " + e.getMessage(), "SALSUP");
        }
    }

    // ADJSUP ["supplyId","nuevoStock","motivo"] -> ajuste
    public String handleAdjustStock(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [supplyId,nuevoStock,motivo]", "ADJSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            BigDecimal qty = new BigDecimal(parameters[1].trim());
            String reason = parameters[2].trim();

            supplyService.registerMovement(id, SupplyMovement.MovementType.ADJUSTMENT, qty, null, reason);
            return emailResponseService.generateHeader("ADJSUP") + " Ajuste registrado.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al ajustar stock: " + e.getMessage(), "ADJSUP");
        }
    }

    // MOVSUP ["supplyId"] -> listar movimientos de insumo
    public String handleListMovements(String[] parameters) {
        try {
            if (parameters.length < 1) {
                return emailResponseService.formatErrorResponse("Parámetro [supplyId] faltante.", "MOVSUP");
            }
            Long id = Long.parseLong(parameters[0].trim());
            List<SupplyMovement> movements = supplyService.getMovementsBySupply(id);
            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("MOVSUP"));
            sb.append(" MOVIMIENTOS DE INSUMO\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("Total de movimientos: ").append(movements.size()).append("\n\n");
            int i = 1;
            for (SupplyMovement mv : movements) {
                sb.append(" MOV #").append(i).append("\n");
                sb.append("   • Tipo: ").append(mv.getMovementType()).append("\n");
                sb.append("   • Cantidad: ").append(mv.getQuantity()).append("\n");
                sb.append("   • Fecha: ").append(mv.getDate()).append("\n");
                sb.append("   • Motivo: ").append(mv.getReason()).append("\n\n");
                i++;
            }
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar movimientos: " + e.getMessage(), "MOVSUP");
        }
    }

    // --- Helpers de formato ---
    private String formatSingleSupplyResponse(Supply supply, String message, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        response.append(" ").append(message).append("\n");
        response.append(MINI_SEPARATOR).append("\n");
        response.append("   • ID: ").append(supply.getId()).append("\n");
        response.append("   • Nombre: ").append(supply.getNombre()).append("\n");
        response.append("   • Descripción: ").append(supply.getDescripcion()).append("\n");
        response.append("   • Unidad: ").append(supply.getUnidadMedida()).append("\n");
        response.append("   • Stock Actual: ").append(supply.getStockActual()).append("\n\n");
        response.append("OK\n");
        return response.toString();
    }

    private String formatListSuppliesResponse(List<Supply> supplies, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));
        if (supplies.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron insumos en la base de datos.\n");
        } else {
            response.append(" LISTADO DE INSUMOS\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(supplies.size()).append("\n\n");
            int contador = 1;
            for (Supply s : supplies) {
                response.append(" INSUMO #").append(contador).append("\n");
                response.append("   • ID: ").append(s.getId()).append("\n");
                response.append("   • Nombre: ").append(s.getNombre()).append("\n");
                response.append("   • Unidad: ").append(s.getUnidadMedida()).append("\n");
                response.append("   • Stock: ").append(s.getStockActual()).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }

}
