package vista;

import dao.RepartidorDAO;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PanelRepartidores extends JPanel {

    private JTextField txtId;
    private JTextField txtNombre;
    private JButton btnRegistrar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JTable tblRepartidores;
    private DefaultTableModel modeloTabla;

    private RepartidorDAO repartidorDAO;
    private Runnable onDataChanged;

    public PanelRepartidores() {
        this(null);
    }

    public PanelRepartidores(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        this.repartidorDAO = new RepartidorDAO();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        iniciarComponentes();
        cargarDatosTabla();
    }

    private void iniciarComponentes() {
        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));

        JPanel panelCampos = new JPanel(new GridLayout(2, 2, 8, 8));
        panelCampos.setBorder(BorderFactory.createTitledBorder("Datos del Repartidor"));

        panelCampos.add(new JLabel("ID (Automático):"));
        txtId = new JTextField();
        txtId.setEditable(false);
        panelCampos.add(txtId);

        panelCampos.add(new JLabel("Nombre Completo:"));
        txtNombre = new JTextField();
        panelCampos.add(txtNombre);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegistrar = new JButton("Registrar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar Selección");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        panelNorte.add(panelCampos, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new Object[]{"ID", "Nombre"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRepartidores = new JTable(modeloTabla);
        tblRepartidores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(tblRepartidores), BorderLayout.CENTER);

        tblRepartidores.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblRepartidores.getSelectedRow() != -1) {
                int fila = tblRepartidores.getSelectedRow();
                txtId.setText(modeloTabla.getValueAt(fila, 0).toString());
                txtNombre.setText(modeloTabla.getValueAt(fila, 1).toString());
            }
        });

        btnRegistrar.addActionListener(e -> registrarRepartidor());
        btnEditar.addActionListener(e -> editarRepartidor());
        btnEliminar.addActionListener(e -> eliminarRepartidor());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void registrarRepartidor() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del repartidor es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Repartidor r = new Repartidor(0, nombre);
        if (repartidorDAO.create(r)) {
            JOptionPane.showMessageDialog(this, "Repartidor registrado exitosamente.");
            limpiarFormulario();
            cargarDatosTabla();
            notificarCambio();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el repartidor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarRepartidor() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repartidor para editar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(txtId.getText());
        Repartidor r = new Repartidor(id, nombre);
        if (repartidorDAO.update(r)) {
            JOptionPane.showMessageDialog(this, "Repartidor actualizado correctamente.");
            limpiarFormulario();
            cargarDatosTabla();
            notificarCambio();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el registro.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarRepartidor() {
        if (txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un repartidor para eliminar.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Eliminar repartidor?", "Confirmación", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            int id = Integer.parseInt(txtId.getText());
            if (repartidorDAO.delete(id)) {
                JOptionPane.showMessageDialog(this, "Repartidor eliminado.");
                limpiarFormulario();
                cargarDatosTabla();
                notificarCambio();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el repartidor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        List<Repartidor> lista = repartidorDAO.readAll();
        for (Repartidor r : lista) {
            modeloTabla.addRow(new Object[]{r.getId(), r.getNombre()});
        }
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        tblRepartidores.clearSelection();
    }

    private void notificarCambio() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}