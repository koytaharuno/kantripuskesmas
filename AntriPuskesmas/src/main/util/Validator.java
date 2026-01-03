/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.util;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author koyta
 */
public class Validator {

    public static boolean validateAntrianForm(
            JTextField txtNama,
            JTextField txtUmur,
            JRadioButton radioL,
            JRadioButton radioP,
            JTextField txtTinggi,
            JTextField txtBerat,
            JTextArea txtGejala,
            JComboBox<String> comboDokter
    ) {

        // Nama
        if (txtNama.getText().trim().isEmpty()) {
            showError("Nama pasien tidak boleh kosong", txtNama);
            return false;
        }

        // Umur
        int umur;
        try {
            umur = Integer.parseInt(txtUmur.getText());
            if (umur <= 0 || umur > 120) {
                showError("Umur harus antara 1 - 120", txtUmur);
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Umur harus berupa angka", txtUmur);
            return false;
        }

        // Jenis kelamin
        if (!radioL.isSelected() && !radioP.isSelected()) {
            JOptionPane.showMessageDialog(null, "Pilih jenis kelamin");
            return false;
        }

        // Tinggi
        int tinggi;
        try {
            tinggi = Integer.parseInt(txtTinggi.getText());
            if (tinggi < 50 || tinggi > 250) {
                showError("Tinggi badan tidak valid", txtTinggi);
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Tinggi badan harus berupa angka", txtTinggi);
            return false;
        }

        // Berat
        int berat;
        try {
            berat = Integer.parseInt(txtBerat.getText());
            if (berat < 2 || berat > 300) {
                showError("Berat badan tidak valid", txtBerat);
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Berat badan harus berupa angka", txtBerat);
            return false;
        }

        // Gejala
        if (txtGejala.getText().trim().isEmpty()) {
            showError("Gejala tidak boleh kosong", txtGejala);
            return false;
        }

        return true;
    }

    // Helper method (encapsulation)
    private static void showError(String message, javax.swing.JComponent comp) {
        JOptionPane.showMessageDialog(null, message);
        comp.requestFocus();
    }
}
