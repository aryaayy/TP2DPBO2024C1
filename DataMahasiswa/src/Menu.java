import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.sql.*;
import java.util.Objects;

public class Menu extends JFrame{
    public static void main(String[] args) {
        // buat object window
        Menu window = new Menu();

        // atur ukuran window
        window.setSize(480, 560);

        // letakkan window di tengah layar
        window.setLocationRelativeTo(null);

        // isi window
        window.setContentPane(window.mainPanel);

        // ubah warna background
        window.getContentPane().setBackground(Color.white);

        // tampilkan window
        window.setVisible(true);

        // agar program ikut berhenti saat window diclose
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // index baris yang diklik
    private int selectedIndex = -1;
    // list untuk menampung semua mahasiswa
    private ArrayList<Mahasiswa> listMahasiswa;
    private Database database;

    private JPanel mainPanel;
    private JTextField nimField;
    private JTextField namaField;
    private JTable mahasiswaTable;
    private JButton addUpdateButton;
    private JButton cancelButton;
    private JComboBox jenisKelaminComboBox;
    private JButton deleteButton;
    private JLabel titleLabel;
    private JLabel nimLabel;
    private JLabel namaLabel;
    private JLabel jenisKelaminLabel;
    private JComboBox kelasComboBox;
    private JLabel kelasLabel;

    // constructor
    public Menu() {
        // inisialisasi listMahasiswa
        listMahasiswa = new ArrayList<>();

        // isi listMahasiswa
//        populateList();
        database = new Database();

        // isi tabel mahasiswa
        mahasiswaTable.setModel(setTable());

        // ubah styling title
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        // atur isi combo box
        String[] jenisKelaminData = {"", "Laki-laki", "Perempuan"};
        jenisKelaminComboBox.setModel(new DefaultComboBoxModel(jenisKelaminData));

        // isi combo box atribut baru (prodi)
        String[] kelasData = {"", "C1", "C2"};
        kelasComboBox.setModel(new DefaultComboBoxModel(kelasData));

        // sembunyikan button delete
        deleteButton.setVisible(false);

        // saat tombol add/update ditekan
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedIndex == -1){
                    insertData();
                }
                else{
                    updateData();
                }
            }
        });
        // saat tombol delete ditekan
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(selectedIndex >= 0){
                    deleteData();
                }

            }
        });
        // saat tombol cancel ditekan
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        // saat salah satu baris tabel ditekan
        mahasiswaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // ubah selectedIndex menjadi baris tabel yang diklik
                selectedIndex = mahasiswaTable.getSelectedRow();

                // simpan value textfield dan combo box
                String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
                String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();
                String selectedJenisKelamin = mahasiswaTable.getModel().getValueAt(selectedIndex, 3).toString();
                String selectedKelas = mahasiswaTable.getModel().getValueAt(selectedIndex, 4).toString();

                // ubah isi textfield dan combo box
                nimField.setText(selectedNim);
                namaField.setText(selectedNama);
                jenisKelaminComboBox.setSelectedItem(selectedJenisKelamin);
                kelasComboBox.setSelectedItem(selectedKelas);

                // ubah button "Add" menjadi "Update"
                addUpdateButton.setText("Update");
                // tampilkan button delete
                deleteButton.setVisible(true);
            }
        });
    }

    public final DefaultTableModel setTable() {
        // tentukan kolom tabel
        Object[] column = {"No", "NIM", "Nama", "Jenis Kelamin", "Kelas"};

        // buat objek tabel dengan kolom yang sudah dibuat
        DefaultTableModel temp = new DefaultTableModel(null, column);

        try {
            // ambil data dari database
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            int i = 0;
            while(resultSet.next()){
                Object[] row = new Object[5];
                row[0] = i+1;
                row[1] = resultSet.getString("nim");
                row[2] = resultSet.getString("nama");
                row[3] = resultSet.getString("jenis_kelamin");
                row[4] = resultSet.getString("kelas");

                temp.addRow(row);
                i++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return temp; // return juga harus diganti
    }

    public void insertData() {
        // ambil value dari textfield dan combobox
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = kelasComboBox.getSelectedItem().toString();

        // cek apakah NIM sudah terdaftar atau belum
        boolean validNim = !nimExist(nim);
        // cek apakah ada kolom form yang kosong
        boolean validForm = !nim.isEmpty() && !nama.isEmpty() && !jenisKelamin.isEmpty() && !kelas.isEmpty();

        if(validNim && validForm) { // tambahkan data ke dalam database jika NIM belum terdaftar dan tidak ada form yang kosong
            String sql = "INSERT INTO mahasiswa VALUES (null, '" + nim + "', '" + nama + "', '" + jenisKelamin + "', '" + kelas + "');";
            database.insertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Insert berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil ditambahkan");
        }
        else if(!validForm){ // message error jika ada form kosong
            JOptionPane.showMessageDialog(null, "Insert gagal! Ada data yang kosong");
        }
        else if(!validNim){ // message error jika NIM sudah terdaftar
            JOptionPane.showMessageDialog(null, "Insert gagal! NIM sudah terdaftar");
        }
    }

    public void updateData() {
        // ambil data asli
        String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
        String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();

        // ambil data dari form
        String nim = nimField.getText();
        String nama = namaField.getText();
        String jenisKelamin = jenisKelaminComboBox.getSelectedItem().toString();
        String kelas = kelasComboBox.getSelectedItem().toString();

        // cek apakah ada kolom form yang kosong
        boolean validForm = !nim.isEmpty() && !nama.isEmpty() && !jenisKelamin.isEmpty() && !kelas.isEmpty();

        if(validForm) { // ubah data mahasiswa di database jika tidak ada form yang kosong
            String sql = "UPDATE mahasiswa SET nim = '" + nim + "', nama = '" + nama + "', jenis_kelamin = '" + jenisKelamin + "', kelas = '" + kelas + "' WHERE nim = '" + selectedNim + "' AND nama = '" + selectedNama + "';";
            database.insertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Update berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil diubah!");
        }
        else{ // message error jika ada form yang kosong
            JOptionPane.showMessageDialog(null, "Update gagal! Ada data yang kosong");
        }
    }

    public void deleteData() {
        // ambil data asli
        String selectedNim = mahasiswaTable.getModel().getValueAt(selectedIndex, 1).toString();
        String selectedNama = mahasiswaTable.getModel().getValueAt(selectedIndex, 2).toString();

        // konfirmasi penghapusan
        int confirmDel = JOptionPane.showConfirmDialog(null, "Yakin ingin menghapus data?", "Konfirmasi penghapusan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if(confirmDel == 0) { // jika mengkonfirmasi penghapusan data
            // hapus data dari database
            String sql = "DELETE FROM mahasiswa WHERE nim = '" + selectedNim + "' AND nama = '" + selectedNama + "';";
            database.insertUpdateDeleteQuery(sql);

            // update tabel
            mahasiswaTable.setModel(setTable());

            // bersihkan form
            clearForm();

            // feedback
            System.out.println("Delete berhasil!");
            JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
        }
        else{ // jika tidak mengkonfirmasi penghapusan data
            // bersihkan form
            clearForm();
        }
    }

    public boolean nimExist(String nim){
        try {
            // ambil data dari database
            ResultSet resultSet = database.selectQuery("SELECT * FROM mahasiswa");

            while(resultSet.next()){
                // cek apakah nim data baru sama dengan data yang sudah ada
                if(Objects.equals(resultSet.getString("nim"), nim)){
                    // return true jika ada NIM yang sama
                    return true;
                }
            }

            // return false jika tidak ada NIM yang sama
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearForm() {
        // kosongkan semua texfield dan combo box
        nimField.setText("");
        namaField.setText("");
        jenisKelaminComboBox.setSelectedItem("");
        kelasComboBox.setSelectedItem("");

        // ubah button "Update" menjadi "Add"
        addUpdateButton.setText("Add");
        // sembunyikan button delete
        deleteButton.setVisible(false);
        // ubah selectedIndex menjadi -1 (tidak ada baris yang dipilih)
        selectedIndex = -1;
    }
}
