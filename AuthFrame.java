package JavaProject;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthFrame extends JFrame {

    JTextField txtEmail = new JTextField(20);

    JPasswordField txtMdp = new JPasswordField(20);

    public AuthFrame() {

        setTitle("Connexion");
        setSize(280, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new FlowLayout());

        add(new JLabel("Email :"));
        add(txtEmail);
        add(new JLabel("Mot de passe :"));
        add(txtMdp);

        JButton btnConnecter = new JButton("Se connecter");
        JButton btnCreer     = new JButton("Creer un compte");
        add(btnConnecter);
        add(btnCreer);

        btnConnecter.addActionListener(e -> connecter());
        btnCreer.addActionListener(e -> inscrire());
    }

    private void connecter() {

        String email = txtEmail.getText();
        String mdp   = new String(txtMdp.getPassword());

        try {
            Connection connexion = Database.getConnection();

            String requete = "SELECT * FROM utilisateurs WHERE email = ? AND password = ?";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, email);
            stmt.setString(2, mdp);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                Session.emailConnecte = email;
                Session.roleConnecte  = res.getString("role");
                new MenuFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants invalides.");
            }

            res.close();
            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void inscrire() {

        try {
            Connection connexion = Database.getConnection();

            String requete = "INSERT INTO utilisateurs (email, password, role) VALUES (?, ?, 'client')";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, txtEmail.getText());
            stmt.setString(2, new String(txtMdp.getPassword()));

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Compte cree avec succes !");

            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}