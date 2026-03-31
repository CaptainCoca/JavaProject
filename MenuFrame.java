package JavaProject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MenuFrame extends JFrame {

    DefaultTableModel model = new DefaultTableModel();

    JTable tableJeux = new JTable(model);

    public MenuFrame() {

        setTitle("Ludotheque - Connecte en tant que : " + Session.emailConnecte);
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        model.setColumnIdentifiers(new String[]{"ID", "Nom", "Entreprise", "Stock", "Etat"});

        chargerLudotheque();

        // Masquer ID et Stock pour les clients
        if (!"admin".equals(Session.roleConnecte)) {
            TableColumn colId    = tableJeux.getColumnModel().getColumn(0);
            TableColumn colStock = tableJeux.getColumnModel().getColumn(3);
            colId.setMinWidth(0);
            colId.setMaxWidth(0);
            colId.setWidth(0);
            colStock.setMinWidth(0);
            colStock.setMaxWidth(0);
            colStock.setWidth(0);
        }

        // Panneau info compte
        String typeCompte = "admin".equals(Session.roleConnecte) ? "Compte admin" : "Compte client";
        JLabel lblCompte = new JLabel(Session.emailConnecte + "  |  " + typeCompte);
        lblCompte.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Panneau bas gauche : Deconnexion + Recharger
        JButton btnDeconnexion = new JButton("Deconnexion");
        JButton btnRecharger   = new JButton("Recharger");

        JPanel panneauBasGauche = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panneauBasGauche.add(btnDeconnexion);
        panneauBasGauche.add(btnRecharger);

        // Panneau bas client : Emprunter + Acheter + Mes jeux
        JPanel panneauBasDroit = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if (!"admin".equals(Session.roleConnecte)) {
            JButton btnEmprunter = new JButton("Emprunter");
            JButton btnAcheter   = new JButton("Acheter");
            JButton btnMesJeux   = new JButton("Mes jeux");

            panneauBasDroit.add(btnEmprunter);
            panneauBasDroit.add(btnAcheter);
            panneauBasDroit.add(btnMesJeux);

            btnEmprunter.addActionListener(e -> transaction("emprunter"));
            btnAcheter.addActionListener(e   -> transaction("acheter"));
            btnMesJeux.addActionListener(e   -> new MesJeuxFrame(this).setVisible(true));
        }

        JPanel panneauBas = new JPanel(new BorderLayout());
        panneauBas.add(panneauBasGauche, BorderLayout.WEST);
        panneauBas.add(panneauBasDroit,  BorderLayout.EAST);

        // Panneau droite admin : boutons en colonne
        if ("admin".equals(Session.roleConnecte)) {
            JButton btnAjouterJeu    = new JButton("Ajouter jeu");
            JButton btnModifierJeu   = new JButton("Modifier jeu");
            JButton btnSupprimerJeu  = new JButton("Supprimer jeu");
            JButton btnUtilisateurs  = new JButton("Utilisateurs");

            Dimension taille = new Dimension(140, 30);
            btnAjouterJeu.setPreferredSize(taille);
            btnModifierJeu.setPreferredSize(taille);
            btnSupprimerJeu.setPreferredSize(taille);
            btnUtilisateurs.setPreferredSize(taille);

            JPanel panneauDroit = new JPanel();
            panneauDroit.setLayout(new BoxLayout(panneauDroit, BoxLayout.Y_AXIS));
            panneauDroit.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            panneauDroit.add(btnAjouterJeu);
            panneauDroit.add(Box.createVerticalStrut(5));
            panneauDroit.add(btnModifierJeu);
            panneauDroit.add(Box.createVerticalStrut(5));
            panneauDroit.add(btnSupprimerJeu);
            panneauDroit.add(Box.createVerticalGlue());
            panneauDroit.add(btnUtilisateurs);

            btnAjouterJeu.addActionListener(e   -> ajouterJeu());
            btnModifierJeu.addActionListener(e  -> modifierJeu());
            btnSupprimerJeu.addActionListener(e -> supprimerJeu());
            btnUtilisateurs.addActionListener(e -> new UtilisateursFrame().setVisible(true));

            add(panneauDroit, BorderLayout.EAST);
        }

        add(lblCompte,                  BorderLayout.NORTH);
        add(new JScrollPane(tableJeux), BorderLayout.CENTER);
        add(panneauBas,                 BorderLayout.SOUTH);

        btnRecharger.addActionListener(e -> chargerLudotheque());
        btnDeconnexion.addActionListener(e -> {
            Session.emailConnecte = null;
            Session.roleConnecte  = null;
            new AuthFrame().setVisible(true);
            this.dispose();
        });
    }

    public void chargerLudotheque() {
        model.setRowCount(0);

        try {
            Connection connexion = Database.getConnection();

            String requete = "SELECT * FROM jeux_video";

            Statement stmt = connexion.createStatement();
            ResultSet res  = stmt.executeQuery(requete);

            while (res.next()) {
                model.addRow(new Object[]{
                    res.getInt("id"),
                    res.getString("nom"),
                    res.getString("entreprise"),
                    res.getInt("exemplaires"),
                    res.getString("etat")
                });
            }

            res.close();
            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void transaction(String type) {

        int row = tableJeux.getSelectedRow();

        if (row == -1) return;

        int    idJeu  = (int)    model.getValueAt(row, 0);
        String nomJeu = (String) model.getValueAt(row, 1);
        String etat   = (String) model.getValueAt(row, 4);

        if ("indisponible".equals(etat)) {
            JOptionPane.showMessageDialog(this, "Desole, ce jeu n'est pas disponible pour le moment, veuillez reessayer ulterieurement.");
            return;
        }

        try {
            Connection connexion = Database.getConnection();

            String requete1 = "UPDATE jeux_video SET exemplaires = exemplaires - 1 WHERE id = ?";
            PreparedStatement stmt1 = connexion.prepareStatement(requete1);
            stmt1.setInt(1, idJeu);
            stmt1.executeUpdate();

            String requete2 = "UPDATE jeux_video SET etat = 'indisponible' WHERE id = ? AND exemplaires = 0";
            PreparedStatement stmt2 = connexion.prepareStatement(requete2);
            stmt2.setInt(1, idJeu);
            stmt2.executeUpdate();

            String requete3 = "INSERT INTO transactions (user_email, jeu_nom, type_action) VALUES (?, ?, ?)";
            PreparedStatement stmt3 = connexion.prepareStatement(requete3);
            stmt3.setString(1, Session.emailConnecte);
            stmt3.setString(2, nomJeu);
            stmt3.setString(3, type);
            stmt3.executeUpdate();

            stmt1.close();
            stmt2.close();
            stmt3.close();
            connexion.close();

            chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void ajouterJeu() {

        JTextField txtNom         = new JTextField(15);
        JTextField txtEntreprise  = new JTextField(15);
        JTextField txtExemplaires = new JTextField(15);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        panel.add(new JLabel("Entreprise :"));
        panel.add(txtEntreprise);
        panel.add(new JLabel("Exemplaires :"));
        panel.add(txtExemplaires);

        int result = JOptionPane.showConfirmDialog(this, panel, "Ajouter un jeu", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            String requete = "INSERT INTO jeux_video (nom, entreprise, exemplaires, etat) VALUES (?, ?, ?, 'disponible')";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, txtNom.getText());
            stmt.setString(2, txtEntreprise.getText());
            stmt.setInt(3, Integer.parseInt(txtExemplaires.getText()));
            stmt.executeUpdate();

            stmt.close();
            connexion.close();

            chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void modifierJeu() {

        int row = tableJeux.getSelectedRow();

        if (row == -1) return;

        int    idJeu              = (int)    model.getValueAt(row, 0);
        String nomActuel          = (String) model.getValueAt(row, 1);
        String entrepriseActuelle = (String) model.getValueAt(row, 2);
        int    exemplairesActuels = (int)    model.getValueAt(row, 3);

        JTextField txtNom         = new JTextField(nomActuel, 15);
        JTextField txtEntreprise  = new JTextField(entrepriseActuelle, 15);
        JTextField txtExemplaires = new JTextField(String.valueOf(exemplairesActuels), 15);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Nom :"));
        panel.add(txtNom);
        panel.add(new JLabel("Entreprise :"));
        panel.add(txtEntreprise);
        panel.add(new JLabel("Exemplaires :"));
        panel.add(txtExemplaires);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modifier le jeu", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            int nouveauxExemplaires = Integer.parseInt(txtExemplaires.getText());
            String nouvelEtat = nouveauxExemplaires > 0 ? "disponible" : "indisponible";

            String requete = "UPDATE jeux_video SET nom = ?, entreprise = ?, exemplaires = ?, etat = ? WHERE id = ?";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, txtNom.getText());
            stmt.setString(2, txtEntreprise.getText());
            stmt.setInt(3, nouveauxExemplaires);
            stmt.setString(4, nouvelEtat);
            stmt.setInt(5, idJeu);
            stmt.executeUpdate();

            stmt.close();
            connexion.close();

            chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void supprimerJeu() {

        int row = tableJeux.getSelectedRow();

        if (row == -1) return;

        int    idJeu  = (int)    model.getValueAt(row, 0);
        String nomJeu = (String) model.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "Supprimer " + nomJeu + " ?", "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection connexion = Database.getConnection();

            String requete = "DELETE FROM jeux_video WHERE id = ?";
            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setInt(1, idJeu);
            stmt.executeUpdate();

            stmt.close();
            connexion.close();

            chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}