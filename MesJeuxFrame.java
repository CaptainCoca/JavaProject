package JavaProject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MesJeuxFrame extends JFrame {

    DefaultTableModel model = new DefaultTableModel();

    JTable tableTransactions = new JTable(model);

    MenuFrame menuFrame;

    public MesJeuxFrame(MenuFrame menuFrame) {
        this.menuFrame = menuFrame;

        setTitle("Mes jeux");
        setSize(600, 400);
        setLocationRelativeTo(null);

        model.setColumnIdentifiers(new String[]{"ID", "Jeu", "Action", "Date"});

        chargerMesTransactions();

        JButton btnRendre    = new JButton("Rendre / Vendre");
        JButton btnRecharger = new JButton("Recharger");

        JPanel panneauBas = new JPanel();
        panneauBas.add(btnRendre);
        panneauBas.add(btnRecharger);

        add(new JScrollPane(tableTransactions), BorderLayout.CENTER);
        add(panneauBas, BorderLayout.SOUTH);

        btnRendre.addActionListener(e -> rendre());
        btnRecharger.addActionListener(e -> chargerMesTransactions());
    }

    private void chargerMesTransactions() {

        model.setRowCount(0);

        try {
            Connection connexion = Database.getConnection();

            String requete = "SELECT * FROM transactions WHERE user_email = ?";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, Session.emailConnecte);

            ResultSet res = stmt.executeQuery();

            while (res.next()) {
                model.addRow(new Object[]{
                    res.getInt("id"),
                    res.getString("jeu_nom"),
                    res.getString("type_action"),
                    res.getTimestamp("date_action")
                });
            }

            // Fermeture des ressources
            res.close();
            stmt.close();
            connexion.close();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    private void rendre() {

        int row = tableTransactions.getSelectedRow();

        if (row == -1) return;

        int    idTransaction = (int)    model.getValueAt(row, 0);
        String nomJeu        = (String) model.getValueAt(row, 1);

        try {
            Connection connexion = Database.getConnection();

            String requete1 = "DELETE FROM transactions WHERE id = ?";
            PreparedStatement stmt1 = connexion.prepareStatement(requete1);
            stmt1.setInt(1, idTransaction);
            stmt1.executeUpdate();

            String requete2 = "UPDATE jeux_video SET exemplaires = exemplaires + 1 WHERE nom = ?";
            PreparedStatement stmt2 = connexion.prepareStatement(requete2);
            stmt2.setString(1, nomJeu);
            stmt2.executeUpdate();

            stmt1.close();
            stmt2.close();
            connexion.close();

            chargerMesTransactions();

            menuFrame.chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

}