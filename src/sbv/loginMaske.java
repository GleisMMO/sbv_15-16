package sbv;

import java.util.logging.Level;
import static sbv.Sbv.logger;

public class loginMaske extends javax.swing.JFrame {

    public loginMaske() {
        initComponents();
    }

    private static String args[];

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPasswordField1 = new javax.swing.JPasswordField();
        username = new javax.swing.JTextField();
        usernameText = new javax.swing.JLabel();
        passwordText = new javax.swing.JLabel();
        login = new javax.swing.JButton();
        password = new javax.swing.JPasswordField();
        registerBut = new javax.swing.JButton();

        jPasswordField1.setText("jPasswordField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Bitte einloggen");
        setLocation(new java.awt.Point(650, 350));

        username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameActionPerformed(evt);
            }
        });

        usernameText.setText("Benutzername");

        passwordText.setText("Passwort");

        login.setText("Einloggen");
        login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginActionPerformed(evt);
            }
        });

        password.setEchoChar('*');
        password.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordActionPerformed(evt);
            }
        });

        registerBut.setText("Account Registrieren");
        registerBut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                registerButActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(registerBut)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(login))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(username, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(password, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(username, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameText, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(passwordText, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(login)
                    .addComponent(registerBut))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void loginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginActionPerformed
        String user = username.getText();
        String pw = password.getText();
        Query.getConnection();
        try {
            if (!Query.anyQuery("SELECT ID_Ben "
                    + "FROM sbm_benutzerverwaltung "
                    + "WHERE Benutzer LIKE '" + user + "' "
                    + "AND Passwort = '" + pw + "';").isEmpty()) {
                Oberflaeche.main(args, user, (Query.anyQuery("SELECT Lizenz "
                        + "FROM sbm_benutzerverwaltung "
                        + "WHERE Benutzer LIKE '" + user + "' "
                        + "AND Passwort = '" + pw + "' ;").get(0)));
                logger.log(Level.SEVERE, "login of the User ''{0}''", user);
                setVisible(false);
            } else if ((user.equalsIgnoreCase("Admin") && pw.equals("1234"))) {
                Oberflaeche.main(args, user, "0");
                logger.log(Level.SEVERE, "login of the local Admin");
                setVisible(false);
            } else {
                Query.disconnect();
                Other.errorWin("Benutzername oder Passwort falsch !");
                logger.log(Level.SEVERE, "false login of the User ''{0}''", user);
            } // end of if-else
        } catch (Exception e) {
            System.out.println(e + " => Anmeldung");
            logger.log(Level.WARNING, "Exception ''{0}''", new Object[]{e});
        }


    }//GEN-LAST:event_loginActionPerformed

    private void passwordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordActionPerformed
        loginActionPerformed(evt);
    }//GEN-LAST:event_passwordActionPerformed

    private void registerButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_registerButActionPerformed
        register.main(args);
    }//GEN-LAST:event_registerButActionPerformed

    private void usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameActionPerformed
        loginActionPerformed(evt);
    }//GEN-LAST:event_usernameActionPerformed

    public static void main(String args[]) {
        if (false) {
            Query.getConnection();
            Oberflaeche.main(args, "Admin", "0");
        } else {
            loginMaske.args = args;
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new loginMaske().setVisible(true);
                }
            });
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JButton login;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordText;
    private javax.swing.JButton registerBut;
    private javax.swing.JTextField username;
    private javax.swing.JLabel usernameText;
    // End of variables declaration//GEN-END:variables
}
