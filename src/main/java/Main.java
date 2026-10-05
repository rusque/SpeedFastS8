import util.ConexionDB;
import vista.MainFrame;

import javax.swing.*;
import java.sql.Connection;

/**
 * Clase principal que inicializa la aplicación SpeedFast.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Establecer el Look and Feel nativo del sistema operativo
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                 IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("No se pudo cargar el Look and Feel del sistema: " + e.getMessage());
        }

        // 2. Verificar la conectividad con la base de datos antes de mostrar la UI
        try (Connection conn = ConexionDB.getConexion()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("-> Conexión exitosa a la base de datos SpeedFast.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo conectar a la base de datos.\n" +
                            "Asegúrese de que el servidor MySQL esté iniciado y la base 'speedfast_db' exista.\n\n" +
                            "Detalle: " + ex.getMessage(),
                    "Advertencia de Conexión",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        // 3. Ejecutar la ventana principal en el hilo seguro de Swing (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame ventana = new MainFrame();
            ventana.setVisible(true);
        });
    }
}