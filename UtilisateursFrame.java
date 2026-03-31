package JavaProject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UtilisateursFrame extends JFrame {

    DefaultTableModel modelClients = new DefaultTableModel();
    DefaultTableModel modelAdmins  = new DefaultTableModel();

    JTable tableClients = new JTable(modelClients);
    JTable tableAdmins  = new JTable(modelAdmins);

    public UtilisateursFrame() {

        setTitle("Gestion des utilisateurs");
        setSize(700, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        modelClients.setColumnIdentifiers(new String[]{"ID", "Email"});
        modelAdmins.setColumnIdentifiers(new String[]{"ID", "Email"});

        chargerUtilisateurs();

        // Masquer colonnes ID
        TableColumn colIdClients = tableClients.getColumnModel().getColumn(0);
        colIdClients.setMinWidth(0);
        colIdClients.setMaxWidth(0);
        colIdClients.setWidth(0);

        TableColumn colIdAdmins = tableAdmins.getColumnModel().getColumn(0);
        colIdAdmins.setMinWidth(0);
        colIdAdmins.setMaxWidth(0);
        colIdAdmins.setWidth(0);

        // Boutons clients
        JButton btnAjouterClient    = new JButton("Ajouter client");
        JButton btnModifierClient   = new JButton("Modifier client");
        JButton btnSupprimerClient  = new JButton("Supprimer client");

        Dimension taille = new Dimension(150, 30);
        btnAjouterClient.setPreferredSize(taille);
        btnModifierClient.setPreferredSize(taille);
        btnSupprimerClient.setPreferredSize(taille);

        JPanel panneauBoutonsClients = new JPanel();
        panneauBoutonsClients.setLayout(new BoxLayout(panneauBoutonsClients, BoxLayout.Y_AXIS));
        panneauBoutonsClients.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panneauBoutonsClients.add(btnAjouterClient);
        panneauBoutonsClients.add(Box.createVerticalStrut(5));
        panneauBoutonsClients.add(btnModifierClient);
        panneauBoutonsClients.add(Box.createVerticalStrut(5));
        panneauBoutonsClients.add(btnSupprimerClient);

        // Boutons admins
        JButton btnAjouterAdmin    = new JButton("Ajouter admin");
        JButton btnModifierAdmin   = new JButton("Modifier admin");
        JButton btnSupprimerAdmin  = new JButton("Supprimer admin");

        btnAjouterAdmin.setPreferredSize(taille);
        btnModifierAdmin.setPreferredSize(taille);
        btnSupprimerAdmin.setPreferredSize(taille);

        JPanel panneauBoutonsAdmins = new JPanel();
        panneauBoutonsAdmins.setLayout(new BoxLayout(panneauBoutonsAdmins, BoxLayout.Y_AXIS));
        panneauBoutonsAdmins.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panneauBoutonsAdmins.add(btnAjouterAdmin);
        panneauBoutonsAdmins.add(Box.createVerticalStrut(5));
        panneauBoutonsAdmins.add(btnModifierAdmin);
        panneauBoutonsAdmins.add(Box.createVerticalStrut(5));
        panneauBoutonsAdmins.add(btnSupprimerAdmin);

        // Panneau clients
        JPanel panneauClients = new JPanel(new BorderLayout());
        panneauClients.setBorder(BorderFactory.createTitledBorder("Clients"));
        panneauClients.add(new JScrollPane(tableClients), BorderLayout.CENTER);
        panneauClients.add(panneauBoutonsClients, BorderLayout.SOUTH);

        // Panneau admins
        JPanel panneauAdmins = new JPanel(new BorderLayout());
        panneauAdmins.setBorder(BorderFactory.createTitledBorder("Admins"));
        panneauAdmins.add(new JScrollPane(tableAdmins), BorderLayout.CENTER);
        panneauAdmins.add(panneauBoutonsAdmins, BorderLayout.SOUTH);

        // Panneau principal
        JPanel panneauPrincipal = new JPanel(new GridLayout(1, 2, 10, 0));
        panneauPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panneauPrincipal.add(panneauClients);
        panneauPrincipal.add(panneauAdmins);

        add(panneauPrincipal, BorderLayout.CENTER);

        btnAjouterClient.addActionListener(e   -> ajouterCompte("client"));
        btnModifierClient.addActionListener(e  -> modifierCompte(tableClients, modelClients));
        btnSupprimerClient.addActionListener(e -> supprimerCompte(tableClients, modelClients));

        btnAjouterAdmin.addActionListener(e   -> ajouterCompte("admin"));
        btnModifierAdmin.addActionListener(e  -> modifierCompte(tableAdmins, modelAdmins));
        btnSupprimerAdmin.addActionListener(e -> supprimerCompte(tableAdmins, modelAdmins));
    }

    private void chargerUtilisateurs() {

        modelClients.setRowCount(0);
        modelAdmins.setRowCount(0);

        try {
            Connection connexion = Database.getConnection();

            Statement stmt = connexion.createStatement();
            ResultSet res  = stmt.executeQuery("SELECT id, email, role FROM utilisateurs");

            while (res.next()) {
                int    id    = res.getInt("id");
                String email = res.getString("email");
                String role  = res.getString("role");

                if ("client".equals(role)) {
                    modelClients.addRow(new Object[]{id, email});
                } else if ("admin".equals(role)) {
                    modelAdmins.addRow(new Object[]{id, email});
                }
            }

            res.close();
            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void ajouterCompte(String role) {

        JTextField txtEmail = new JTextField(20);
        JTextField txtMdp   = new JTextField(20);

        String titre = "admin".equals(role) ? "Ajouter un admin" : "Ajouter un client";

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Email :"));
        panel.add(txtEmail);
        panel.add(new JLabel("Mot de passe :"));
        panel.add(txtMdp);

        int result = JOptionPane.showConfirmDialog(this, panel, titre, JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            String requete = "INSERT INTO utilisateurs (email, password, role) VALUES (?, ?, ?)";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, txtEmail.getText());
            stmt.setString(2, txtMdp.getText());
            stmt.setString(3, role);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Compte " + role + " cree avec succes !");

            stmt.close();
            connexion.close();

            chargerUtilisateurs();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void modifierCompte(JTable table, DefaultTableModel model) {

        int row = table.getSelectedRow();

        if (row == -1) return;

        int    idUtilisateur = (int)    model.getValueAt(row, 0);
        String emailActuel   = (String) model.getValueAt(row, 1);

        JTextField txtEmail = new JTextField(emailActuel, 20);
        JTextField txtMdp   = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Email :"));
        panel.add(txtEmail);
        panel.add(new JLabel("Nouveau mot de passe :"));
        panel.add(txtMdp);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifier le compte", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            String requete = "UPDATE utilisateurs SET email = ?, password = ? WHERE id = ?";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, txtEmail.getText());
            stmt.setString(2, txtMdp.getText());
            stmt.setInt(3, idUtilisateur);
            stmt.executeUpdate();

            stmt.close();
            connexion.close();

            chargerUtilisateurs();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void supprimerCompte(JTable table, DefaultTableModel model) {

        int row = table.getSelectedRow();

        if (row == -1) return;

        int    idUtilisateur = (int)    model.getValueAt(row, 0);
        String email         = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer " + email + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            String requete = "DELETE FROM utilisateurs WHERE id = ?";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setInt(1, idUtilisateur);
            stmt.executeUpdate();

            stmt.close();
            connexion.close();

            chargerUtilisateurs();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}