package vista;

import dao.PedidoDAO;
import modelo.Pedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class PanelPedidos extends JPanel {

    private JTextField txtId;
    private JTextField txtDireccion;
    private JComboBox<String> cmbTipo;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbFiltroEstado;

    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnFiltrar;
    private JButton btnVerTodos;

    private JTable tblPedidos;
    private DefaultTableModel modeloTabla;

    private PedidoDAO pedidoDAO;

    public PanelPedidos() {
        this.pedidoDAO = new PedidoDAO();
        initComponents();
        cargarDatosTabla(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Formulario Superior
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Gestión de Pedidos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0;
        pnlForm.add(new JLabel("ID:"), gbc);
        txtId = new JTextField(12);
        txtId.setEditable(false);
        gbc.gridx = 1; gbc.gridy = 0;
        pnlForm.add(txtId, gbc);

        // Dirección
        gbc.gridx = 0; gbc.gridy = 1;
        pnlForm.add(new JLabel("Dirección:*"), gbc);
        txtDireccion = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        pnlForm.add(txtDireccion, gbc);

        // Tipo ENUM
        gbc.gridx = 0; gbc.gridy = 2;
        pnlForm.add(new JLabel("Tipo:"), gbc);
        cmbTipo = new JComboBox<>(new String[]{"COMIDA", "ENCOMIENDA", "EXPRESS"});
        gbc.gridx = 1; gbc.gridy = 2;
        pnlForm.add(cmbTipo, gbc);

        // Estado ENUM
        gbc.gridx = 0; gbc.gridy = 3;
        pnlForm.add(new JLabel("Estado:"), gbc);
        cmbEstado = new JComboBox<>(new String[]{"PENDIENTE", "EN_REPARTO", "ENTREGADO"});
        gbc.gridx = 1; gbc.gridy = 3;
        pnlForm.add(cmbEstado, gbc);

        // Botones Formulario
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        btnRegistrar = new JButton("Registrar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");
        pnlBotones.add(btnRegistrar);
        pnlBotones.add(btnEditar);
        pnlBotones.add(btnEliminar);
        pnlBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        pnlForm.add(pnlBotones, gbc);

        // Filtro Opcional
        JPanel pnlFiltro = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlFiltro.add(new JLabel("Filtrar por Estado:"));
        cmbFiltroEstado = new JComboBox<>(new String[]{"PENDIENTE", "EN_REPARTO", "ENTREGADO"});
        pnlFiltro.add(cmbFiltroEstado);
        btnFiltrar = new JButton("Filtrar");
        btnVerTodos = new JButton("Ver Todos");
        pnlFiltro.add(btnFiltrar);
        pnlFiltro.add(btnVerTodos);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        pnlForm.add(pnlFiltro, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // Tabla
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Dirección", "Tipo", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblPedidos = new JTable(modeloTabla);
        tblPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblPedidos), BorderLayout.CENTER);

        // Listeners
        btnRegistrar.addActionListener(e -> registrarPedido());
        btnEditar.addActionListener(e -> editarPedido());
        btnEliminar.addActionListener(e -> eliminarPedido());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnFiltrar.addActionListener(e -> cargarDatosTabla((String) cmbFiltroEstado.getSelectedItem()));
        btnVerTodos.addActionListener(e -> cargarDatosTabla(null));

        tblPedidos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
    }

    public void cargarDatosTabla(String filtroEstado) {
        modeloTabla.setRowCount(0);
        try {
            List<Pedido> lista = pedidoDAO.readAll();
            for (Pedido p : lista) {
                if (filtroEstado == null || p.getEstado().equalsIgnoreCase(filtroEstado)) {
                    modeloTabla.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar pedidos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPedido() {
        String dir = txtDireccion.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La dirección es obligatoria.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pedido p = new Pedido(0, dir, (String) cmbTipo.getSelectedItem(), (String) cmbEstado.getSelectedItem());
        try {
            if (pedidoDAO.create(p)) {
                JOptionPane.showMessageDialog(this, "Pedido registrado con éxito.");
                cargarDatosTabla(null);
                limpiarCampos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al registrar pedido: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarPedido() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dir = txtDireccion.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La dirección es obligatoria.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText());
            Pedido p = new Pedido(id, dir, (String) cmbTipo.getSelectedItem(), (String) cmbEstado.getSelectedItem());
            if (pedidoDAO.update(p)) {
                JOptionPane.showMessageDialog(this, "Pedido actualizado con éxito.");
                cargarDatosTabla(null);
                limpiarCampos();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar pedido: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPedido() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el pedido seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (resp == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText());
                if (pedidoDAO.delete(id)) {
                    JOptionPane.showMessageDialog(this, "Pedido eliminado con éxito.");
                    cargarDatosTabla(null);
                    limpiarCampos();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar: el pedido puede estar referenciado en una entrega.\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int fila = tblPedidos.getSelectedRow();
        if (fila != -1) {
            txtId.setText(tblPedidos.getValueAt(fila, 0).toString());
            txtDireccion.setText(tblPedidos.getValueAt(fila, 1).toString());
            cmbTipo.setSelectedItem(tblPedidos.getValueAt(fila, 2).toString());
            cmbEstado.setSelectedItem(tblPedidos.getValueAt(fila, 3).toString());
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        tblPedidos.clearSelection();
    }
}