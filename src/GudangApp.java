import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class GudangApp {    
    // ✅ Perbaiki URL koneksi
    private static final String URL = "jdbc:mysql://localhost:3306/gudang?useSSL=false&serverTimezone=UTC";
    private static final String USER = "javauser"; // atau "root"
    private static final String PASSWORD = "password123";

     public static void main(String[] args) {
        new GudangApp();
    }

      private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtNama, txtStok, txtHarga, txtJumlahTransaksi;
    private JButton btnTambah, btnEdit, btnHapus, btnTransaksi, btnLihatTransaksi;

    public GudangApp() {
        frame = new JFrame("🏢 Aplikasi Gudang");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Panel Tabel Barang
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Stok", "Harga"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        loadBarang();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("📜 Daftar Barang"));

        // Panel Input Barang
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("🏷️ Kelola Barang"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelForm.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1;
        txtNama = new JTextField(15);
        panelForm.add(txtNama, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelForm.add(new JLabel("Stok:"), gbc);
        gbc.gridx = 1;
        txtStok = new JTextField(15);
        panelForm.add(txtStok, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelForm.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1;
        txtHarga = new JTextField(15);
        panelForm.add(txtHarga, gbc);

        // Panel Tombol Barang
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnTambah = new JButton("Tambah");
        btnTambah.addActionListener(e -> tambahBarang());
        btnEdit = new JButton("Edit");
        btnEdit.addActionListener(e -> editBarang());
        btnHapus = new JButton("Hapus");
        btnHapus.addActionListener(e -> hapusBarang());
        panelButton.add(btnTambah);
        panelButton.add(btnEdit);
        panelButton.add(btnHapus);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panelForm.add(panelButton, gbc);

        // Panel Transaksi
        JPanel panelTransaksi = new JPanel(new GridBagLayout());
        panelTransaksi.setBorder(BorderFactory.createTitledBorder("💰 Transaksi"));

        // Panel Tombol Transaksi
        JPanel panelButtonTransaksi = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnTransaksi = new JButton("Proses Transaksi");
        btnTransaksi.addActionListener(e -> prosesTransaksi());
        btnLihatTransaksi = new JButton("Lihat Transaksi");
        btnLihatTransaksi.addActionListener(e -> loadTransaksi());
        panelButtonTransaksi.add(btnTransaksi);
        panelButtonTransaksi.add(btnLihatTransaksi);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panelTransaksi.add(panelButtonTransaksi, gbc);

        // Tambahkan ke Frame
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panelForm, BorderLayout.WEST);
        frame.add(panelTransaksi, BorderLayout.EAST);
        frame.setVisible(true);

        // Cek Driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL ditemukan!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL tidak ditemukan!");
            e.printStackTrace();
        }
    }


    private void loadBarang() {
        model.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM barang")) {

            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("nama"), rs.getInt("stok"), rs.getDouble("harga")});
            }
        } catch (SQLException e) {
            showError("Gagal memuat data", e);
        }
    }
    
        private void editBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Pilih barang yang ingin diedit", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String nama = txtNama.getText().trim();
        String stokText = txtStok.getText().trim();
        String hargaText = txtHarga.getText().trim();

        if (nama.isEmpty() || stokText.isEmpty() || hargaText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stok;
        double harga;
        try {
            stok = Integer.parseInt(stokText);
            harga = Double.parseDouble(hargaText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Stok dan harga harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE barang SET nama = ?, stok = ?, harga = ? WHERE id = ?")) {

            stmt.setString(1, nama);
            stmt.setInt(2, stok);
            stmt.setDouble(3, harga);
            stmt.setInt(4, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Barang berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadBarang();
            clearFields();
        } catch (SQLException e) {
            showError("Gagal memperbarui barang", e);
        }
    }

    private void tambahBarang() {
        String nama = txtNama.getText().trim();
        String stokText = txtStok.getText().trim();
        String hargaText = txtHarga.getText().trim();

        if (nama.isEmpty() || stokText.isEmpty() || hargaText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stok;
        double harga;
        try {
            stok = Integer.parseInt(stokText);
            harga = Double.parseDouble(hargaText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Stok dan harga harus berupa angka!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO barang (nama, stok, harga) VALUES (?, ?, ?)")) {

            stmt.setString(1, nama);
            stmt.setInt(2, stok);
            stmt.setDouble(3, harga);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Barang berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadBarang();
            clearFields();
        } catch (SQLException e) {
            showError("Gagal menambahkan barang", e);
        }
    }
    private void prosesTransaksi() {
        String idText = txtNama.getText().trim();
        String jumlahText = txtStok.getText().trim();

        if (idText.isEmpty() || jumlahText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, " Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int barangId = Integer.parseInt(idText);
            int jumlah = Integer.parseInt(jumlahText);

            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement checkStmt = conn.prepareStatement("SELECT stok FROM barang WHERE id = ?");
                checkStmt.setInt(1, barangId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int stokSaatIni = rs.getInt("stok");

                    if (stokSaatIni >= jumlah) {
                        PreparedStatement updateStokStmt = conn.prepareStatement("UPDATE barang SET stok = stok - ? WHERE id = ?");
                        updateStokStmt.setInt(1, jumlah);
                        updateStokStmt.setInt(2, barangId);
                        updateStokStmt.executeUpdate();

                        PreparedStatement transaksiStmt = conn.prepareStatement("INSERT INTO transaksi (barang_id, jumlah) VALUES (?, ?)");
                        transaksiStmt.setInt(1, barangId);
                        transaksiStmt.setInt(2, jumlah);
                        transaksiStmt.executeUpdate();

                        conn.commit();
                        JOptionPane.showMessageDialog(frame, "✅ Transaksi berhasil!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                        loadBarang();
                    } else {
                        JOptionPane.showMessageDialog(frame, "⚠️ Stok tidak mencukupi!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            showError("❌ Gagal memproses transaksi", e);
        }
    }
    
    private void loadTransaksi() {
        DefaultTableModel modelTransaksi = new DefaultTableModel(new String[]{"ID", "Barang", "Jumlah", "Tanggal"}, 0);
        JTable tableTransaksi = new JTable(modelTransaksi);

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT t.id, b.nama, t.jumlah, t.tanggal FROM transaksi t " +
                     "JOIN barang b ON t.barang_id = b.id")) {

            while (rs.next()) {
                modelTransaksi.addRow(new Object[]{rs.getInt("id"), rs.getString("nama"), rs.getInt("jumlah"), rs.getTimestamp("tanggal")});
            }
        } catch (SQLException e) {
            showError("❌ Gagal memuat data transaksi", e);
        }

        JFrame frameTransaksi = new JFrame("Riwayat Transaksi");
        frameTransaksi.setSize(500, 300);
        frameTransaksi.add(new JScrollPane(tableTransaksi), BorderLayout.CENTER);
        frameTransaksi.setVisible(true);
    }

    private void hapusBarang() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Pilih barang yang ingin dihapus", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(frame, "Apakah Anda yakin ingin menghapus barang ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM barang WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(frame, "Barang berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadBarang();
        } catch (SQLException e) {
            showError("Gagal menghapus barang", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void clearFields() {
        txtNama.setText("");
        txtStok.setText("");
        txtHarga.setText("");
    }

    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(frame, message + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

}




