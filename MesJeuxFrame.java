import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

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
        setResizable(false);

        model.setColumnIdentifiers(new String[]{"ID", "Jeu", "Action", "Date"});

        // Masquer la colonne ID
        chargerMesTransactions();

        TableColumn colId = tableTransactions.getColumnModel().getColumn(0);
        colId.setMinWidth(0);
        colId.setMaxWidth(0);
        colId.setWidth(0);

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

            String requete = "SELECT * FROM transactions WHERE user_identifiant = ?";

            PreparedStatement stmt = connexion.prepareStatement(requete);
            stmt.setString(1, Session.identifiantConnecte);

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

        // Ouvre la fenêtre de notation
        int note = afficherDialogueNote(nomJeu);
        if (note == -1) return;

        try {
            Connection connexion = Database.getConnection();

            // Enregistre la note dans la table "avis"
            String requeteAvis = "INSERT INTO avis (jeu_nom, user_identifiant, note) VALUES (?, ?, ?)";
            PreparedStatement stmtAvis = connexion.prepareStatement(requeteAvis);
            stmtAvis.setString(1, nomJeu);
            stmtAvis.setString(2, Session.identifiantConnecte);
            stmtAvis.setInt(3, note);
            stmtAvis.executeUpdate();
            stmtAvis.close();

            // Recalcule la moyenne de TOUS les avis du jeu
            // et l'enregistre directement dans le champ "moyenne"de la table "jeux_vidéo"
            String requeteMoyenne = "UPDATE jeux_video SET moyenne = " +
                                    "(SELECT AVG(note) FROM avis WHERE jeu_nom = ?) WHERE nom = ?";
            PreparedStatement stmtMoyenne = connexion.prepareStatement(requeteMoyenne);
            stmtMoyenne.setString(1, nomJeu);
            stmtMoyenne.setString(2, nomJeu);
            stmtMoyenne.executeUpdate();
            stmtMoyenne.close();

            // Supprime la ligne de transaction
            String requete1 = "DELETE FROM transactions WHERE id = ?";
            PreparedStatement stmt1 = connexion.prepareStatement(requete1);
            stmt1.setInt(1, idTransaction);
            stmt1.executeUpdate();
            stmt1.close();

            // Remet l'exemplaire en stock
            String requete2 = "UPDATE jeux_video SET exemplaires = exemplaires + 1 WHERE nom = ?";
            PreparedStatement stmt2 = connexion.prepareStatement(requete2);
            stmt2.setString(1, nomJeu);
            stmt2.executeUpdate();
            stmt2.close();

            // Repasse le jeu en "disponible" si le stock est maintenant > 0
            String requete3 = "UPDATE jeux_video SET etat = 'disponible' WHERE nom = ? AND exemplaires > 0";
            PreparedStatement stmt3 = connexion.prepareStatement(requete3);
            stmt3.setString(1, nomJeu);
            stmt3.executeUpdate();
            stmt3.close();

            connexion.close();

            // Rafraîchit les deux tableaux
            chargerMesTransactions();
            menuFrame.chargerLudotheque();

        } catch (SQLException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Affiche une boîte de dialogue avec 5 étoiles cliquables.
     * Retourne la note choisie, ou -1 si annulé.
     */
    private int afficherDialogueNote(String nomJeu) {
        final int[] noteSelectionnee = {0};

        JLabel[] etoiles = new JLabel[5]; 
        JLabel   lblNote = new JLabel("Aucune note", SwingConstants.CENTER);

        JPanel panneauEtoiles = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));

        for (int i = 0; i < 5; i++) {
            final int index = i;
            etoiles[i] = new JLabel("☆");
            etoiles[i].setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 32));
            etoiles[i].setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            etoiles[i].setForeground(java.awt.Color.GRAY);
            panneauEtoiles.add(etoiles[i]);

            etoiles[i].addMouseListener(new java.awt.event.MouseAdapter() {

                // allume en orange toutes les étoiles jusqu'à celle survolée
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    for (int j = 0; j < 5; j++) {
                        etoiles[j].setText(j <= index ? "★" : "☆");
                        etoiles[j].setForeground(j <= index ? new java.awt.Color(255, 180, 0) : java.awt.Color.GRAY);
                    }
                }

                // Revient à l'état de la note actuellement sélectionnée
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    for (int j = 0; j < 5; j++) {
                        etoiles[j].setText(j < noteSelectionnee[0] ? "★" : "☆");
                        etoiles[j].setForeground(j < noteSelectionnee[0] ? new java.awt.Color(255, 180, 0) : java.awt.Color.GRAY);
                    }
                }

                // Enregistre la note 
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    noteSelectionnee[0] = index + 1;
                    lblNote.setText(noteSelectionnee[0] + " / 5");
                    for (int j = 0; j < 5; j++) {
                        etoiles[j].setText(j < noteSelectionnee[0] ? "★" : "☆");
                        etoiles[j].setForeground(j < noteSelectionnee[0] ? new java.awt.Color(255, 180, 0) : java.awt.Color.GRAY);
                    }
                }
            });
        }

        // Création de la fenetre
        JPanel panneauDialogue = new JPanel(new BorderLayout(0, 8));
        panneauDialogue.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panneauDialogue.add(new JLabel("Notez « " + nomJeu + " » :", SwingConstants.CENTER), BorderLayout.NORTH);
        panneauDialogue.add(panneauEtoiles, BorderLayout.CENTER);
        panneauDialogue.add(lblNote, BorderLayout.SOUTH); // affiche "X / 5" sous les étoiles

        int result = JOptionPane.showConfirmDialog(
            this, panneauDialogue,
            "Donner une note", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        // Si l'utilisateur clique Annuler ou ferme la fenêtre, on retourne -1
        if (result != JOptionPane.OK_OPTION) return -1;
        return noteSelectionnee[0];
    }

}