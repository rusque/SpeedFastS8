package vista;

import dao.RepartidorDAO;
import dao.EntregaDAO;
import dao.PedidoDAO;
import modelo.Entrega;
import modelo.Pedido;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PanelEntregas extends JPanel {

    private JTextField txtId;
    private JComboBox<PedidoItem> cmbPedido;
    private JComboBox<RepartidorItem> cmbRepartidor;
    private JTextField txtFecha; // Formato YYYY-MM-DD
    private JTextField txtHora;  // Formato HH:MM:SS

    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    private JTable tblEntregas;
    private DefaultTableModel modeloTabla;

    private EntregaDAO entregaDAO;
    private PedidoDAO pedidoDAO;
    private RepartidorDAO repartidorDAO;

    // Clases auxiliares para mapear IDs en los combos
    public static class PedidoItem {
        private int id;
        private String descripcion;

        public PedidoItem(int id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        public int getId() { return id; }

        @Override
        public String toString() {
            return id + " - " + descripcion;
        }
    }

    public static class RepartidorItem {
        private int id;
        private String nombre;

        public RepartidorItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }

        @Override
        public String toString() {
            return id + " - " + nombre;
        }
    }

    public PanelEntregas() {
        this.entregaDAO = new EntregaDAO();
        this.pedidoDAO = new PedidoDAO();
        this.repartidorDAO = new RepartidorDAO();

        initComponents();
        refrescarCombos();
        cargarDatosTabla();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gestión de Entregas"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("ID:"), gbc);
        txtId = new JTextField(10);
        txtId.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        pnlForm.add(txtId, gbc);

        // Combo Pedido
        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(new JLabel("Pedido:*"), gbc);
        cmbPedido = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1;
        pnlForm.add(cmbPedido, gbc);

        // Combo Repartidor
        gbc.gridx = 0; gbc.gridy = 2;
        pnlForm.add(new JLabel("Repartidor:*"), gbc);
        cmbRepartidor = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 2;
        pnlForm.add(cmbRepartidor, gbc);

        // Fecha
        gbc.gridx = 0; gbc.gridy = 3;
        pnlForm.add(new JLabel("Fecha (YYYY-MM-DD):*"), gbc);
        txtFecha = new JTextField(LocalDate.now().toString(), 12);
        gbc.gridx = 1; gbc.gridy = 3;
        pnlForm.add(txtFecha, gbc);

        // Hora
        gbc.gridx = 0; gbc.gridy = 4;
        pnlForm.add(new JLabel("Hora (HH:MM:SS):*"), gbc);
        txtHora = new JTextField(LocalTime.now().withNano(0).toString(), 12);
        gbc.gridx = 1; gbc.gridy = 4;
        pnlForm.add(txtHora, gbc);

        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnRegistrar = new JButton("Registrar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        pnlBotones.add(btnRegistrar);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "ID Pedido", "ID Repartidor", "Fecha", "Hora"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblEntregas = new JTable(modeloTabla);
        tblEntregas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblEntregas), BorderLayout.CENTER);

        // Listeners
        btnRegistrar.addActionListener(e -> registrarEntrega());
        btnEditar.addActionListener(e -> editarEntrega());
        btnEliminar.addActionListener(e -> eliminarEntrega());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tblEntregas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
    }

    public void refrescarCombos() {
        cmbPedido.removeAllItems();
        try {
            List<Pedido> pedidos = pedidoDAO.readAll();
            for (Pedido p : pedidos) {
                cmbPedido.addItem(new PedidoItem(p.getId(), p.getDireccion() + " (" + p.getTipo() + ")"));
            }
        } catch (Exception ignored) {}

        cmbRepartidor.removeAllItems();
        try {
            List<Repartidor> reps = repartidorDAO.readAll();
            for (Repartidor r : reps) {
                cmbRepartidor.addItem(new RepartidorItem(r.getId(), r.getNombre()));
            }
        } catch (Exception ignored) {}
    }

    public void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Entrega> lista = entregaDAO.readAll();
            for (Entrega ent : lista) {
                modeloTabla.addRow(new Object[]{
                        ent.getId(),
                        ent.getIdPedido(),
                        ent.getIdRepartidor(),
                        ent.getFecha(),
                        ent.getHora()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar entregas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarEntrega() {
        PedidoItem ped = (PedidoItem) cmbPedido.getSelectedItem();
        RepartidorItem rep = (RepartidorItem) cmbRepartidor.getSelectedItem();

        if (ped == null || rep == null) {
            JOptionPane.showMessageDialog(this, "Debe registrar pedidos y repartidores antes de crear una entrega.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Date fecha = Date.valueOf(txtFecha.getText().trim());
            Time hora = Time.valueOf(txtHora.getText().trim());

            Entrega ent = new Entrega(0, ped.getId(), rep.getId(), fecha, hora);
            if (entregaDAO.create(ent)) {
                JOptionPane.showMessageDialog(this, "Entrega registrada exitosamente.");
                cargarDatosTabla();
                limpiarCampos();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha u hora incorrecto.\nUse YYYY-MM-DD para fecha y HH:MM:SS para hora.", "Error de Formato", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar entrega: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarEntrega() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una entrega de la tabla para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        PedidoItem ped = (PedidoItem) cmbPedido.getSelectedItem();
        RepartidorItem rep = (RepartidorItem) cmbRepartidor.getSelectedItem();

        try {
            int id = Integer.parseInt(txtId.getText());
            Date fecha = Date.valueOf(txtFecha.getText().trim());
            Time hora = Time.valueOf(txtHora.getText().trim());

            Entrega ent = new Entrega(id, ped.getId(), rep.getId(), fecha, hora);
            if (entregaDAO.update(ent)) {
                JOptionPane.showMessageDialog(this, "Entrega actualizada exitosamente.");
                cargarDatosTabla();
                limpiarCampos();
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha u hora incorrecto.", "Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar entrega: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEntrega() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione una entrega para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar esta entrega?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                if (entregaDAO.delete(id)) {
                    JOptionPane.showMessageDialog(this, "Entrega eliminada exitosamente.");
                    cargarDatosTabla();
                    limpiarCampos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int fila = tblEntregas.getSelectedRow();
        if (fila != -1) {
            txtId.setText(tblEntregas.getValueAt(fila, 0).toString());
            int idPed = Integer.parseInt(tblEntregas.getValueAt(fila, 1).toString());
            int idRep = Integer.parseInt(tblEntregas.getValueAt(fila, 2).toString());

            // Seleccionar en combos
            for (int i = 0; i < cmbPedido.getItemCount(); i++) {
                if (cmbPedido.getItemAt(i).getId() == idPed) {
                    cmbPedido.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < cmbRepartidor.getItemCount(); i++) {
                if (cmbRepartidor.getItemAt(i).getId() == idRep) {
                    cmbRepartidor.setSelectedIndex(i);
                    break;
                }
            }

            txtFecha.setText(tblEntregas.getValueAt(fila, 3).toString());
            txtHora.setText(tblEntregas.getValueAt(fila, 4).toString());
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().withNano(0).toString());
        tblEntregas.clearSelection();
    }
}