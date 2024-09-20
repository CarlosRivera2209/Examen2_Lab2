package examen2_parcial2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {

    private static PSNUsers psnUsers;

    public static void main(String[] args) {
        try {
            psnUsers = new PSNUsers("psn.dat");
            crearInterfazPrincipal();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public static void crearInterfazPrincipal() {
        JFrame frame = new JFrame("PSN Management 🎮");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("PSN MANAGEMENT 🎮", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(75, 75, 255));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton addUserButton = new JButton("Agregar usuario");
        JButton deactivateUserButton = new JButton("Desactivar usuario");
        JButton addTrophyButton = new JButton("Agregar trofeo");
        JButton viewInfoButton = new JButton("Ver información de jugador");
        JButton exitButton = new JButton("Salir");

        styleButton(addUserButton);
        styleButton(deactivateUserButton);
        styleButton(addTrophyButton);
        styleButton(viewInfoButton);
        styleButton(exitButton);

        addUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarUsuario();
            }
        });

        deactivateUserButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desactivarUsuario();
            }
        });

        addTrophyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                agregarTrofeo();
            }
        });

        viewInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                verInfoJugador();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        panel.add(addUserButton);
        panel.add(deactivateUserButton);
        panel.add(addTrophyButton);
        panel.add(viewInfoButton);
        panel.add(exitButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(75, 75, 255));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static void agregarUsuario() {
        String username = JOptionPane.showInputDialog("Ingrese el nombre de usuario:");

        if (username != null && !username.isEmpty()) {
            try {
                long pos = psnUsers.users.search(username);
                if (pos != -1L) {
                    JOptionPane.showMessageDialog(null, "El nombre de usuario ya existe (activo o desactivado). Intente con otro.", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    psnUsers.addUser(username);
                    JOptionPane.showMessageDialog(null, "Usuario agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al agregar el usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void desactivarUsuario() {
        String username = JOptionPane.showInputDialog("Ingrese el nombre de usuario a desactivar:");
        if (username != null && !username.isEmpty()) {
            try {
                long pos = psnUsers.users.search(username);
                if (pos == -1L) {
                    JOptionPane.showMessageDialog(null, "Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                psnUsers.raf.seek(pos);
                String userRead = psnUsers.raf.readUTF();
                boolean isActive = psnUsers.raf.readBoolean();

                if (!isActive) {
                    JOptionPane.showMessageDialog(null, "La cuenta ya está desactivada.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                psnUsers.deactivateUser(username);
                JOptionPane.showMessageDialog(null, "Usuario desactivado exitosamente.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al desactivar usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacío.");
        }
    }

    private static void agregarTrofeo() {
        String username = JOptionPane.showInputDialog("Ingrese el nombre de usuario:");
        if (username != null && !username.isEmpty()) {
            long pos = psnUsers.users.search(username);
            if (pos == -1L) {
                JOptionPane.showMessageDialog(null, "Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                psnUsers.raf.seek(pos);
                String userRead = psnUsers.raf.readUTF();
                boolean isActive = psnUsers.raf.readBoolean();
                if (!isActive) {
                    JOptionPane.showMessageDialog(null, "El usuario está desactivado.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error al acceder a los datos del usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String trophyGame = JOptionPane.showInputDialog("Ingrese el nombre del juego:");
            String trophyName = JOptionPane.showInputDialog("Ingrese el nombre del trofeo:");
            String[] options = {"PLATINO", "ORO", "PLATA", "BRONCE"};
            String type = (String) JOptionPane.showInputDialog(null, "Seleccione el tipo de trofeo:",
                    "Tipo de trofeo", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (trophyGame != null && trophyName != null && type != null) {
                try {
                    psnUsers.addTrophieTo(username, trophyGame, trophyName, Trophy.valueOf(type));
                    JOptionPane.showMessageDialog(null, "Trofeo agregado exitosamente.");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error al agregar trofeo: " + e.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void verInfoJugador() {
        String username = JOptionPane.showInputDialog("Ingrese el nombre de usuario:");
        if (username != null && !username.isEmpty()) {
            try {
                StringBuilder playerInfo = new StringBuilder();
                long pos = psnUsers.users.search(username);
                if (pos == -1L) {
                    JOptionPane.showMessageDialog(null, "Usuario no encontrado.");
                    return;
                }

                psnUsers.raf.seek(pos);
                playerInfo.append("Username: ").append(psnUsers.raf.readUTF()).append("\n");
                boolean isActive = psnUsers.raf.readBoolean();
                if (!isActive) {
                    JOptionPane.showMessageDialog(null, "El usuario está desactivado.");
                    return;
                }

                int points = psnUsers.raf.readInt();
                int trophies = psnUsers.raf.readInt();
                playerInfo.append("Puntos: ").append(points).append("\n");
                playerInfo.append("Trofeos: ").append(trophies).append("\n\n");

                playerInfo.append("Trofeos ganados:\n");

                try (RandomAccessFile rafTrophies = new RandomAccessFile("psn.tro", "r")) {
                    rafTrophies.seek(0);
                    while (rafTrophies.getFilePointer() < rafTrophies.length()) {
                        String user = rafTrophies.readUTF();
                        String type = rafTrophies.readUTF();
                        String game = rafTrophies.readUTF();
                        String trophy = rafTrophies.readUTF();
                        String date = rafTrophies.readUTF();

                        if (user.equals(username)) {
                            playerInfo.append(" Fecha: ").append(date)
                                    .append(" - Tipo: ").append(type)
                                    .append(" - Juego: ").append(game)
                                    .append(" - Nombre del trofeo: ").append(trophy)
                                    .append("\n");
                        }
                    }
                }

                if (playerInfo.length() == 0) {
                    playerInfo.append("No se han ganado trofeos.");
                }
                
                JOptionPane.showMessageDialog(null, playerInfo.toString(), "Información del jugador", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error al obtener información del jugador: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "El nombre de usuario no puede estar vacío.");
        }
    }

}
