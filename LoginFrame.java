package JavaProject;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class LoginFrame extends JFrame {
    
    public LoginFrame() {
        setTitle("Test Connexion Laragon");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new FlowLayout());

        JButton btnConnect = new JButton("Tester la connexion BDD");
        JLabel lblStatus = new JLabel("Statut : En attente...");

        btnConnect.addActionListener(e -> {
            try {
                Connection con = Database.getConnection();
                if (con != null) {
                    lblStatus.setText("Statut : Connecté avec succès !");
                    lblStatus.setForeground(Color.GREEN);
                    con.close();
                }
            } catch (Exception ex) {
                lblStatus.setText("Erreur : " + ex.getMessage());
                lblStatus.setForeground(Color.RED);
                ex.printStackTrace();
            }
        });

        add(btnConnect);
        add(lblStatus);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}