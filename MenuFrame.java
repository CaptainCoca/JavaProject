package JavaProject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        model.setColumnIdentifiers(new String[]{"ID", "Nom", "Entreprise", "Stock", "Etat"});

        chargerLudotheque();

        JButton btnEmprunter = new JButton("Emprunter");
        JButton btnAcheter   = new JButton("Acheter");
        JButton btnMesJeux   = new JButton("Mes jeux");
        JButton btnRecharger = new JButton("Recharger");

        JPanel panneauBas = new JPanel();
        panneauBas.add(btnEmprunter);
        panneauBas.add(btnAcheter);
        panneauBas.add(btnMesJeux);
        panneauBas.add(btnRecharger);

        add(new JScrollPane(tableJeux), BorderLayout.CENTER);
        add(panneauBas, BorderLayout.SOUTH);

        btnEmprunter.addActionListener(e -> transaction("emprunter"));
        btnAcheter.addActionListener(e -> transaction("acheter"));
        btnRecharger.addActionListener(e -> chargerLudotheque());

        btnMesJeux.addActionListener(e -> new MesJeuxFrame(this).setVisible(true));
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

        try {
            Connection connexion = Database.getConnection();

            String requete1 = "UPDATE jeux_video SET exemplaires = exemplaires - 1 WHERE id = ?";
            PreparedStatement stmt1 = connexion.prepareStatement(requete1);
            stmt1.setInt(1, idJeu);
            stmt1.executeUpdate();

            String requete2 = "INSERT INTO transactions (user_email, jeu_nom, type_action) VALUES (?, ?, ?)";
            PreparedStatement stmt2 = connexion.prepareStatement(requete2);
            stmt2.setString(1, Session.emailConnecte);
            stmt2.setString(2, nomJeu);
            stmt2.setString(3, type);
            stmt2.executeUpdate();

            stmt1.close();
            stmt2.close();
            connexion.close();

            chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}