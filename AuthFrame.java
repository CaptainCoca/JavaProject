package JavaProject;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class AuthFrame extends JFrame {

    JTextField txtIdentifiant = new JTextField(20);

    JPasswordField txtMdp = new JPasswordField(20);

    public AuthFrame() {

        setTitle("Connexion");
        setSize(250, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new FlowLayout());

        add(new JLabel("Identifiant :"));
        add(txtIdentifiant);
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

        String identifiant = txtIdentifiant.getText();
        String mdp         = new String(txtMdp.getPassword());

        try {
            Connection connexion = Database.getConnection();

            String requete = "SELECT * FROM utilisateurs WHERE identifiant = ?";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, identifiant);

            ResultSet res = stmt.executeQuery();

            if (res.next()) {
                String hashBdd = res.getString("password");

                BCrypt.Result resultat = BCrypt.verifyer().verify(mdp.toCharArray(), hashBdd);

                if (resultat.verified) {
                    Session.identifiantConnecte = identifiant;
                    Session.roleConnecte        = res.getString("role");
                    new MenuFrame().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Identifiants invalides.");
                }
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

        String identifiant = txtIdentifiant.getText().trim();
        String mdp         = new String(txtMdp.getPassword()).trim();

        if (identifiant.isEmpty() || mdp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir un identifiant et un mot de passe.");
            return;
        }

        if (identifiant.length() < 5) {
            JOptionPane.showMessageDialog(this, "L'identifiant doit contenir au moins 5 caracteres.");
            return;
        }

        try {
            Connection connexion = Database.getConnection();

            String mdpHache = BCrypt.withDefaults().hashToString(12, mdp.toCharArray());

            String requete = "INSERT INTO utilisateurs (identifiant, password, role) VALUES (?, ?, 'client')";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, identifiant);
            stmt.setString(2, mdpHache);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Compte cree avec succes !");

            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}