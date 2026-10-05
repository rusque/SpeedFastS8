package vista;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JTabbedPane pestanias;
    private PanelRepartidores panelRepartidores;
    private PanelPedidos panelPedidos;
    private PanelEntregas panelEntregas;

    public MainFrame() {
        super("SpeedFast - Sistema de Gestión de Pedidos y Entregas");
        configurarVentana();
        iniciarComponentes();
    }

    private void configurarVentana() {
        setSize(850, 650);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void iniciarComponentes() {
        pestanias = new JTabbedPane();

        panelRepartidores = new PanelRepartidores();
        panelPedidos = new PanelPedidos();
        panelEntregas = new PanelEntregas();

        pestanias.addTab("Repartidores", panelRepartidores);
        pestanias.addTab("Pedidos", panelPedidos);
        pestanias.addTab("Entregas", panelEntregas);

        // Cuando el usuario cambia a la pestaña de entregas, refrescar los combos de Pedidos y Repartidores
        pestanias.addChangeListener(e -> {
            if (pestanias.getSelectedComponent() == panelEntregas) {
                panelEntregas.refrescarCombos();
                panelEntregas.cargarDatosTabla();
            }
        });

        getContentPane().add(pestanias, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Look & Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}