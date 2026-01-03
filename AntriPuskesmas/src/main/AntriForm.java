/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.ButtonGroup;
import java.nio.file.Path;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

// ========== iText 7 Core (PDF) ==========
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

// ========== iText Layout (Text, Paragraph, Image, Styles) ==========
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

// ========== ZXing (QR Code) ==========
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import main.util.Validator;




/**
 *
 * @author koyta
 */
public class AntriForm extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger
            .getLogger(AntriForm.class.getName());

    int selectedId = -1;
    ButtonGroup genderGroup = new ButtonGroup();

    /**
     * Creates new form AntriForm
     */

    public void loadTable() {
        DefaultTableModel model = new DefaultTableModel();

        model.addColumn("ID");
        model.addColumn("No Antrian");
        model.addColumn("Nama");
        model.addColumn("Umur");
        model.addColumn("JK");
        model.addColumn("Tinggi (cm)");
        model.addColumn("Berat (kg)");
        model.addColumn("Dokter");
        model.addColumn("Gejala");
        model.addColumn("Status");
        model.addColumn("Waktu Daftar");

        try {
            Connection conn = DBConnection.getConnection();
            String sql = "SELECT id_antrian, no_antrian, nama, umur, jenis_kelamin, tinggi_cm, berat_kg, dokter_pilihan, gejala, status, waktu_daftar FROM antrian_pasien ORDER BY no_antrian ASC";
            PreparedStatement pst = conn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id_antrian"),
                        rs.getInt("no_antrian"),
                        rs.getString("nama"),
                        rs.getInt("umur"),
                        rs.getString("jenis_kelamin"),
                        rs.getInt("tinggi_cm"),
                        rs.getInt("berat_kg"),
                        rs.getString("dokter_pilihan"),
                        rs.getString("gejala"),
                        rs.getString("status"),
                        rs.getTimestamp("waktu_daftar")
                });
            }

            jTable1.setModel(model);

            jTable1.getColumnModel().getColumn(0).setMinWidth(0);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(0).setWidth(0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load tabel: " + e.getMessage());
        }
    }

    public void clearForm() {
        jTextField1.setText("");
        jTextField2.setText("");
        genderGroup.clearSelection();
        jTextField3.setText("");
        jTextField4.setText("");
        jTextArea1.setText("");
        jComboBox1.setSelectedIndex(0);
    }

    public void groupButton() {
        genderGroup.add(jRadioButton1);
        genderGroup.add(jRadioButton2);
    }

    private void insertData() {
        if (!Validator.validateAntrianForm(
            jTextField1,
            jTextField2,
            jRadioButton1,
            jRadioButton2,
            jTextField3,
            jTextField4,
            jTextArea1,
            jComboBox1
        )) {
            return;
        }
        
        try {
            String sqlMax = "SELECT COALESCE(MAX(no_antrian), 0) + 1 AS next_no FROM antrian_pasien";
            Connection conn = DBConnection.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sqlMax);

            int nextNo = 1;
            if (rs.next()) {
                nextNo = rs.getInt("next_no");
            }

            String nama = jTextField1.getText();
            int umur = Integer.parseInt(jTextField2.getText());
            String jk = jRadioButton1.isSelected() ? "L" : "P";
            int tinggi = Integer.parseInt(jTextField3.getText());
            int berat = Integer.parseInt(jTextField4.getText());
            String gejala = jTextArea1.getText();
            String dokter = jComboBox1.getSelectedItem().toString();

            String sql = "INSERT INTO antrian_pasien "
                    + "(no_antrian, nama, umur, jenis_kelamin, tinggi_cm, berat_kg, gejala, dokter_pilihan, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pst.setInt(1, nextNo);
            pst.setString(2, nama);
            pst.setInt(3, umur);
            pst.setString(4, jk);
            pst.setInt(5, tinggi);
            pst.setInt(6, berat);
            pst.setString(7, gejala);
            pst.setString(8, dokter);
            pst.setString(9, "Waiting");

            pst.executeUpdate();
            
            ResultSet keys = pst.getGeneratedKeys();
            int newId = -1;

            if (keys.next()) {
                newId = keys.getInt(1);
            }

            // Mulai timer auto call
            startAutoCallTimer(newId);

            JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan!");

            loadTable();
            clearForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menambahkan data: " + e.getMessage());
        }
    }

    public void updateData() {
        if (!Validator.validateAntrianForm(
            jTextField1,
            jTextField2,
            jRadioButton1,
            jRadioButton2,
            jTextField3,
            jTextField4,
            jTextArea1,
            jComboBox1
        )) {
            return;
        }
        
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu!");
            return;
        }

        try {
            String nama = jTextField1.getText();
            int umur = Integer.parseInt(jTextField2.getText());
            String jk = jRadioButton1.isSelected() ? "L" : "P";
            int tinggi = Integer.parseInt(jTextField3.getText());
            int berat = Integer.parseInt(jTextField4.getText());
            String dokter = jComboBox1.getSelectedItem().toString();
            String gejala = jTextArea1.getText();

            String sql = "UPDATE antrian_pasien "
                    + "SET nama=?, umur=?, jenis_kelamin=?, tinggi_cm=?, berat_kg=?, "
                    + "dokter_pilihan=?, gejala=? "
                    + "WHERE id_antrian=?";

            Connection conn = DBConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(sql);

            pst.setString(1, nama);
            pst.setInt(2, umur);
            pst.setString(3, jk);
            pst.setInt(4, tinggi);
            pst.setInt(5, berat);
            pst.setString(6, dokter);
            pst.setString(7, gejala);
            pst.setInt(8, selectedId);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil diperbarui!");

            loadTable();
            clearForm();
            selectedId = -1;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal update data: " + e.getMessage());
        }
    }

    public void deleteData() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang ingin dihapus!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Hapus data ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            String sql = "DELETE FROM antrian_pasien WHERE id_antrian=?";
            Connection conn = DBConnection.getConnection();

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedId);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");

            loadTable();
            clearForm();
            selectedId = -1;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gagal delete: " + e.getMessage());
        }
    }

    public void generateQRCode(String text, String outputPath) {
        try {
            int width = 300;
            int height = 300;

            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix matrix = qrWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            Path path = java.nio.file.FileSystems.getDefault().getPath(outputPath);
            MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "QR Code gagal dibuat: " + e.getMessage());
        }
    }

    public void printSlipAntrian(String noAntrian, String namaPasien, String namaDokter) {
        try {
            // Path QR & PDF
            String qrPath = System.getProperty("user.home") + "/Desktop/QR_" + noAntrian + ".png";
            String pdfPath = System.getProperty("user.home") + "/Desktop/Slip_Antrian_" + noAntrian + ".pdf";

            // 1. Generate QR Code
            generateQRCode(noAntrian, qrPath);

            // 2. Setup PDF
            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A6); // Ukuran slip lebih kecil
            document.setMargins(20, 20, 20, 20);

            // 3. Buat background abu-abu
//            PdfCanvas bg = new PdfCanvas(pdf.addNewPage());
//            bg.setFillColor(ColorConstants.LIGHT_GRAY);
//            bg.rectangle(0, 0, PageSize.A6.getWidth(), PageSize.A6.getHeight());
//            bg.fill();

            // 4. Judul QR Code
            ImageData qrImg = ImageDataFactory.create(qrPath);
            Image qrImage = new Image(qrImg);
            qrImage.setWidth(150);
            qrImage.setHeight(150);
            qrImage.setHorizontalAlignment(HorizontalAlignment.CENTER);

            document.add(qrImage);

            // 5. Nomor Antrian Besar
            Paragraph noText = new Paragraph("NO : " + noAntrian)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(30)
                    .setBold()
                    .setMarginTop(10);
            document.add(noText);

            // 6. Nama Pasien
            Paragraph namaP = new Paragraph("Nama Pasien : " + namaPasien)
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20);
            document.add(namaP);

            // 7. Nama Dokter
            Paragraph namaD = new Paragraph("Nama Dokter : " + namaDokter)
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(5);
            document.add(namaD);

            document.close();

            JOptionPane.showMessageDialog(this,
                    "Slip PDF berhasil dibuat di Desktop!\n" + pdfPath);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal membuat slip PDF: " + e.getMessage());
        }
    }
    
    public void printAllData() {
        try {
            String pdfPath = System.getProperty("user.home") + "/Desktop/Print_All_Antrian.pdf";

            PdfWriter writer = new PdfWriter(pdfPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate()); // landscape
            document.setMargins(20, 20, 20, 20);

        // Judul
            Paragraph title = new Paragraph("Data Antrian Puskesmas")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            int columnCount = jTable1.getColumnCount();
            Table pdfTable = new Table(columnCount);
            pdfTable.setWidth(UnitValue.createPercentValue(100));

        // Header
        for (int col = 0; col < columnCount; col++) {
            pdfTable.addHeaderCell(
                    new Cell().add(new Paragraph(jTable1.getColumnName(col)))
                            .setBold()
            );
        }

        // Isi table
            for (int row = 0; row < jTable1.getRowCount(); row++) {
                for (int col = 0; col < columnCount; col++) {
                    Object value = jTable1.getValueAt(row, col);
                    pdfTable.addCell(new Paragraph(value == null ? "" : value.toString()));
                }
            }

            document.add(pdfTable);
            document.close();

            JOptionPane.showMessageDialog(this,
                "Print All Data selesai!\nFile ada di Desktop.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Gagal Print All Data: " + e.getMessage());
        }
    }

    public void updateStatusToCalled(int idAntrian) {
        try {
            String sql = "UPDATE antrian_pasien SET status='Called' WHERE id_antrian=?";
            Connection conn = DBConnection.getConnection();

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, idAntrian);
            pst.executeUpdate();

            loadTable();

            System.out.println("Status antrian " + idAntrian + " updated to CALLED");
        } catch (Exception e) {
            System.out.println("Error updating status: " + e.getMessage());
        }
    }
    
    public void startAutoCallTimer(int idAntrian) {
        int delay = 30000; // 30 detik

        new javax.swing.Timer(delay, (e) -> {
        updateStatusToCalled(idAntrian);
        }) {{
        setRepeats(false); // hanya jalan sekali
        start();
        }};
    }
    
    public void applyStatusColorRenderer() {
        jTable1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            int statusCol = 9; // SESUAIKAN index STATUS kamu
            String status = table.getValueAt(row, statusCol).toString();

            if (!isSelected) {
                c.setForeground(Color.BLACK);
                c.setBackground(Color.WHITE);
            }

            if (status.equalsIgnoreCase("Called") && !isSelected) {
                c.setBackground(new Color(144, 238, 144)); // hijau muda
            }

            return c;
            }
        });
    }
    
    public void loadTableByNama(String keyword) {
    DefaultTableModel model = new DefaultTableModel();
    
    model.addColumn("ID");
    model.addColumn("No Antrian");
    model.addColumn("Nama");
    model.addColumn("Umur");
    model.addColumn("JK");
    model.addColumn("Tinggi (cm)");
    model.addColumn("Berat (kg)");
    model.addColumn("Dokter");
    model.addColumn("Gejala");
    model.addColumn("Status");
    model.addColumn("Waktu Daftar");

    try {
        Connection conn = DBConnection.getConnection();

        String sql = """
            SELECT id_antrian, no_antrian, nama, umur, jenis_kelamin,
                   tinggi_cm, berat_kg, dokter_pilihan, gejala,
                   status, waktu_daftar
            FROM antrian_pasien
            WHERE nama LIKE ?
            ORDER BY no_antrian ASC
        """;

        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, "%" + keyword + "%");

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id_antrian"),
                rs.getInt("no_antrian"),
                rs.getString("nama"),
                rs.getInt("umur"),
                rs.getString("jenis_kelamin"),
                rs.getInt("tinggi_cm"),
                rs.getInt("berat_kg"),
                rs.getString("dokter_pilihan"),
                rs.getString("gejala"),
                rs.getString("status"),
                rs.getTimestamp("waktu_daftar")
            });
        }

        jTable1.setModel(model);

        // sembunyikan kolom ID
        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal mencari data: " + e.getMessage());
        }
    }



    
    public AntriForm() {
        initComponents();
        groupButton();
        loadTable();
        applyStatusColorRenderer();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jTextField5 = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setFont(new java.awt.Font("Helvetica Neue", 1, 24)); // NOI18N
        jLabel1.setText("Antri Puskesmas");

        jLabel2.setText("Nama :");

        jLabel3.setText("Umur :");

        jLabel4.setText("Jenis Kelamin :");

        jRadioButton1.setText("L");

        jRadioButton2.setText("P");

        jLabel5.setText("Tinggi (cm) :");

        jLabel6.setText("Berat (kg) :");

        jLabel7.setText("Gejala :");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Dr. Konz Ganz", "Drs. Memz Canz" }));

        jLabel8.setText("Dokter :");

        jButton1.setText("Tambah");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setText("Edit");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setText("Hapus");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jButton4.setText("Print Data");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jButton5.setText("Print All Data");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setText("Clear Form");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jTextField5.addActionListener(this::jTextField5ActionPerformed);

        jLabel9.setText("Search By Name :");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jRadioButton1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addGap(18, 18, 18)
                        .addComponent(jButton3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 38, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton4)
                        .addGap(18, 18, 18)
                        .addComponent(jButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 730, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton6)))
                .addGap(0, 11, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jRadioButton1)
                                .addComponent(jRadioButton2)))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(52, 52, 52)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)
                            .addComponent(jButton3)
                            .addComponent(jButton6)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))))
                .addContainerGap(100, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        printAllData();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        clearForm();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
        String keyword = jTextField5.getText();
        loadTableByNama(keyword);
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // TODO add your handling code here:
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih data terlebih dahulu");
            return;
        }

        String noAntrian = jTable1.getValueAt(row, 1).toString();
        String namaPasien = jTable1.getValueAt(row, 2).toString();
        String namaDokter = jTable1.getValueAt(row, 7).toString();

        printSlipAntrian(noAntrian, namaPasien, namaDokter);
    }// GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        deleteData();
    }// GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        insertData();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        int row = jTable1.getSelectedRow();
        if (row < 0)
            return;

        selectedId = Integer.parseInt(jTable1.getValueAt(row, 0).toString());

        jTextField1.setText(jTable1.getValueAt(row, 2).toString());
        jTextField2.setText(jTable1.getValueAt(row, 3).toString());

        String jk = jTable1.getValueAt(row, 4).toString();
        if (jk.equals("L"))
            jRadioButton1.setSelected(true);
        else
            jRadioButton2.setSelected(true);

        jTextField3.setText(jTable1.getValueAt(row, 5).toString());
        jTextField4.setText(jTable1.getValueAt(row, 6).toString());
        jComboBox1.setSelectedItem(jTable1.getValueAt(row, 7).toString());
        jTextArea1.setText(jTable1.getValueAt(row, 8).toString());
    }// GEN-LAST:event_jTable1MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        updateData();
    }// GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        // </editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AntriForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
