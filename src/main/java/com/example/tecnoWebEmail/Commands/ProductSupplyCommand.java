package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.ProductSupply;
import com.example.tecnoWebEmail.Models.Product;
import com.example.tecnoWebEmail.Models.Supply;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.ProductSupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class ProductSupplyCommand {

    @Autowired
    private ProductSupplyService productSupplyService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";

    // ADDSUPP ["productId","supplyId","requiredAmount"]
    public String handleAddSupplyToProduct(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,supplyId,requiredAmount]", "ADDSUPP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Long supplyId = Long.parseLong(parameters[1].trim());
            BigDecimal required = new BigDecimal(parameters[2].trim());

            ProductSupply ps = productSupplyService.addSupplyToProduct(productId, supplyId, required);
            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("ADDSUPP"));
            sb.append(" Insumo agregado al producto con éxito\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("   • ID Relación: ").append(ps.getId()).append("\n");
            sb.append("   • Producto ID: ").append(ps.getProduct().getId()).append("\n");
            sb.append("   • Insumo ID: ").append(ps.getSupply().getId()).append("\n");
            sb.append("   • Cantidad requerida: ").append(ps.getRequiredAmount()).append("\n\n");
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al agregar insumo al producto: " + e.getMessage(), "ADDSUPP");
        }
    }

    // REMSUPP ["productId","supplyId"]
    public String handleRemoveSupplyFromProduct(String[] parameters) {
        try {
            if (parameters.length < 2) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 2: [productId,supplyId]", "REMSUPP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Long supplyId = Long.parseLong(parameters[1].trim());

            productSupplyService.removeSupplyFromProduct(productId, supplyId);
            return emailResponseService.generateHeader("REMSUPP") + " Insumo removido de producto.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al remover insumo del producto: " + e.getMessage(), "REMSUPP");
        }
    }

    // UPDPSP ["productId","supplyId","newAmount"]
    public String handleUpdateRequiredAmount(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,supplyId,newAmount]", "UPDPSP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Long supplyId = Long.parseLong(parameters[1].trim());
            BigDecimal newAmount = new BigDecimal(parameters[2].trim());

            ProductSupply updated = productSupplyService.updateRequiredAmount(productId, supplyId, newAmount);
            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("UPDPSP"));
            sb.append(" Requisito actualizado.\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("   • ID Relación: ").append(updated.getId()).append("\n");
            sb.append("   • Cantidad requerida: ").append(updated.getRequiredAmount()).append("\n\n");
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al actualizar cantidad requerida: " + e.getMessage(), "UPDPSP");
        }
    }

    // LISPSP ["productId"] -> listar insumos de un producto
    public String handleListSuppliesForProduct(String[] parameters) {
        try {
            if (parameters.length < 1) {
                return emailResponseService.formatErrorResponse("Parámetro [productId] faltante.", "LISPSP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            List<ProductSupply> relations = productSupplyService.getSuppliesForProduct(productId);

            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("LISPSP"));
            sb.append(" LISTADO DE INSUMOS POR PRODUCTO\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("Total de registros: ").append(relations.size()).append("\n\n");
            int i = 1;
            for (ProductSupply ps : relations) {
                sb.append(" REL #").append(i).append("\n");
                sb.append("   • ID Relación: ").append(ps.getId()).append("\n");
                sb.append("   • Insumo ID: ").append(ps.getSupply().getId()).append("\n");
                sb.append("   • Insumo Nombre: ").append(ps.getSupply().getNombre()).append("\n");
                sb.append("   • Cantidad requerida: ").append(ps.getRequiredAmount()).append("\n\n");
                i++;
            }
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar insumos por producto: " + e.getMessage(), "LISPSP");
        }
    }

    // LISUPP ["supplyId"] -> listar productos que usan un insumo
    public String handleListProductsUsingSupply(String[] parameters) {
        try {
            if (parameters.length < 1) {
                return emailResponseService.formatErrorResponse("Parámetro [supplyId] faltante.", "LISUPP");
            }
            Long supplyId = Long.parseLong(parameters[0].trim());
            List<ProductSupply> relations = productSupplyService.getProductsUsingSupply(supplyId);

            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("LISUPP"));
            sb.append(" LISTADO DE PRODUCTOS QUE USAN EL INSUMO\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("Total de registros: ").append(relations.size()).append("\n\n");
            int i = 1;
            for (ProductSupply ps : relations) {
                sb.append(" REL #").append(i).append("\n");
                sb.append("   • ID Relación: ").append(ps.getId()).append("\n");
                sb.append("   • Producto ID: ").append(ps.getProduct().getId()).append("\n");
                sb.append("   • Producto Nombre: ").append(ps.getProduct().getNombre()).append("\n");
                sb.append("   • Cantidad requerida por unidad: ").append(ps.getRequiredAmount()).append("\n\n");
                i++;
            }
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar productos que usan el insumo: " + e.getMessage(), "LISUPP");
        }
    }

    // CALRSU ["productId","quantity"] -> calcular insumos requeridos
    public String handleCalculateRequiredSupplies(String[] parameters) {
        try {
            if (parameters.length < 2) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 2: [productId,quantity]", "CALRSU");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Integer qty = Integer.parseInt(parameters[1].trim());

            Map<Supply, BigDecimal> required = productSupplyService.calculateRequiredSupplies(productId, qty);
            StringBuilder sb = new StringBuilder();
            sb.append(emailResponseService.generateHeader("CALRSU"));
            sb.append(" REQUISITOS DE INSUMOS PARA PRODUCCIÓN\n");
            sb.append(MINI_SEPARATOR).append("\n");
            sb.append("Total de insumos: ").append(required.size()).append("\n\n");
            int i = 1;
            for (Map.Entry<Supply, BigDecimal> entry : required.entrySet()) {
                Supply sup = entry.getKey();
                BigDecimal amt = entry.getValue();
                sb.append(" INS #").append(i).append("\n");
                sb.append("   • Insumo ID: ").append(sup.getId()).append("\n");
                sb.append("   • Nombre: ").append(sup.getNombre()).append("\n");
                sb.append("   • Cantidad requerida total: ").append(amt).append("\n\n");
                i++;
            }
            sb.append("OK\n");
            return sb.toString();
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al calcular insumos requeridos: " + e.getMessage(), "CALRSU");
        }
    }

    // VALSUPP ["productId","quantity"] -> validar disponibilidad
    public String handleValidateSuppliesAvailability(String[] parameters) {
        try {
            if (parameters.length < 2) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 2: [productId,quantity]", "VALSUPP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Integer qty = Integer.parseInt(parameters[1].trim());

            boolean ok = productSupplyService.validateSuppliesAvailability(productId, qty);
            return emailResponseService.generateHeader("VALSUPP") + " Disponibilidad: " + (ok ? "SI" : "NO") + "\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al validar disponibilidad de insumos: " + e.getMessage(), "VALSUPP");
        }
    }

    // CONSUPP ["productId","quantity","productionOrderId"] -> consumir insumos para producción
    public String handleConsumeSuppliesForProduction(String[] parameters) {
        try {
            if (parameters.length < 3) {
                return emailResponseService.formatErrorResponse("Número incorrecto de parámetros. Se esperaban 3: [productId,quantity,productionOrderId]", "CONSUPP");
            }
            Long productId = Long.parseLong(parameters[0].trim());
            Integer qty = Integer.parseInt(parameters[1].trim());
            Long productionOrderId = Long.parseLong(parameters[2].trim());

            productSupplyService.consumeSuppliesForProduction(productId, qty, productionOrderId);
            return emailResponseService.generateHeader("CONSUPP") + " Insumos consumidos para producción.\nOK\n";
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al consumir insumos para producción: " + e.getMessage(), "CONSUPP");
        }
    }

}
