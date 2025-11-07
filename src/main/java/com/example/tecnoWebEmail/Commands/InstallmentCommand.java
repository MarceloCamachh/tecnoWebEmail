package com.example.tecnoWebEmail.Commands;

import com.example.tecnoWebEmail.Models.Installment;
import com.example.tecnoWebEmail.Service.EmailResponseService;
import com.example.tecnoWebEmail.Service.InstallmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InstallmentCommand {

    @Autowired
    private InstallmentService installmentService;

    @Autowired
    private EmailResponseService emailResponseService;

    private static final String MINI_SEPARATOR = "------------------------";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Maneja el comando LISCUP ["orderId"]
     * Lista todas las cuotas de un pedido específico.
     */
    public String handleListInstallmentsByOrder(String[] parameters) {
        String command = "LISCUP";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [orderId] faltante.", command);
            }
            Long orderId = Long.parseLong(parameters[0].trim());

            List<Installment> installments = installmentService.getInstallmentsByOrderId(orderId);
            String title = "CUOTAS DEL PEDIDO #" + orderId;
            return formatListInstallmentResponse(installments, title, command);

        } catch (NumberFormatException e) {
            return emailResponseService.formatErrorResponse("[orderId] debe ser un número.", command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar cuotas por pedido: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando LISVEN []
     * Lista todas las cuotas vencidas que no estén pagadas.
     */
    public String handleListOverdueInstallments() {
        String command = "LISVEN";
        try {
            List<Installment> installments = installmentService.getOverdueInstallments();
            String title = "CUOTAS VENCIDAS Y NO PAGADAS";
            return formatListInstallmentResponse(installments, title, command);
        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar cuotas vencidas: " + e.getMessage(), command);
        }
    }

    /**
     * Maneja el comando LISCES ["estado"]
     * Lista todas las cuotas por un estado específico (Pending, Partial, Paid).
     */
    public String handleListInstallmentsByState(String[] parameters) {
        String command = "LISCES";
        try {
            if (parameters.length < 1 || parameters[0].trim().isEmpty()) {
                return emailResponseService.formatErrorResponse("Parámetro [estado] faltante. (Ej: Pending, Partial, Paid)", command);
            }
            String state = parameters[0].trim();

            List<Installment> installments = installmentService.getInstallmentsByState(state);
            String title = "CUOTAS CON ESTADO: " + state.toUpperCase();
            return formatListInstallmentResponse(installments, title, command);

        } catch (Exception e) {
            return emailResponseService.formatErrorResponse("Error al listar cuotas por estado: " + e.getMessage(), command);
        }
    }


    // --- Helper de Formato de Respuesta ---

    /**
     * Formatea una lista de cuotas en una respuesta de email.
     */
    private String formatListInstallmentResponse(List<Installment> installments, String title, String command) {
        StringBuilder response = new StringBuilder();
        response.append(emailResponseService.generateHeader(command));

        if (installments.isEmpty()) {
            response.append("RESULTADO DEL LISTADO\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append(" No se encontraron cuotas que coincidan con los criterios.\n");
        } else {
            response.append(" ").append(title).append("\n");
            response.append(MINI_SEPARATOR).append("\n");
            response.append("Total de registros encontrados: ").append(installments.size()).append("\n\n");

            int contador = 1;
            for (Installment inst : installments) {
                response.append(" CUOTA #").append(contador).append(" (ID: ").append(inst.getId()).append(")\n");
                response.append("   • Pedido ID: ").append(inst.getOrder().getId()).append("\n");
                response.append("   • Nro. Cuota: ").append(inst.getInstallmentNumber()).append("\n");
                response.append("   • Monto Cuota: ").append(inst.getInstallmentAmount()).append("\n");
                response.append("   • Monto Pagado: ").append(inst.getAmountPaid()).append("\n");

                // Calcular Saldo Pendiente
                BigDecimal pending = inst.getInstallmentAmount().subtract(inst.getAmountPaid());
                response.append("   • Saldo Pendiente: ").append(pending).append("\n");

                response.append("   • Estado: ").append(inst.getState()).append("\n");
                response.append("   • Vencimiento: ").append(inst.getDueDate().format(DATE_FORMATTER)).append("\n\n");
                contador++;
            }
        }
        response.append("OK\n");
        return response.toString();
    }
}