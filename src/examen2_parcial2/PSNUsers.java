package examen2_parcial2;

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class PSNUsers {

    RandomAccessFile raf;
    HashTable users;

    public PSNUsers(String filePath) throws IOException {
        this.raf = new RandomAccessFile(filePath, "rw");
        this.users = new HashTable();
        reloadHashTable();
    }

    private void reloadHashTable() throws IOException {
        raf.seek(0);
        while (raf.getFilePointer() < raf.length()) {
            long pos = raf.getFilePointer();
            String username = raf.readUTF();
            boolean isActive = raf.readBoolean();

            if (isActive) {
                raf.readInt();
                raf.readInt();
                users.add(username, pos);
            } else {
                raf.readInt();
                raf.readInt();
            }
        }
    }

    public void addUser(String username) throws IOException {
        if (users.search(username) != -1L) {
            System.out.println("El username ya existe.");
            return;
        }

        raf.seek(raf.length());
        long pos = raf.getFilePointer();
        raf.writeUTF(username);
        raf.writeBoolean(true);
        raf.writeInt(0);
        raf.writeInt(0);

        users.add(username, pos);
    }

    public void deactivateUser(String username) {
        long pos = users.search(username);
        if (pos != -1L) {
            try {
                raf.seek(pos);
                raf.readUTF();
                raf.writeBoolean(false);

                removeTrophiesForUser(username);
            } catch (Exception e) {
                throw new RuntimeException("Error al desactivar usuario: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Usuario no encontrado.");
        }
    }

    private void removeTrophiesForUser(String username) {
        try (RandomAccessFile rafTrophies = new RandomAccessFile("psn.tro", "r"); RandomAccessFile tempRaf = new RandomAccessFile("psn.tro_temp", "rw")) {

            while (rafTrophies.getFilePointer() < rafTrophies.length()) {
                String user = rafTrophies.readUTF();
                String type = rafTrophies.readUTF();
                String game = rafTrophies.readUTF();
                String trophy = rafTrophies.readUTF();
                String date = rafTrophies.readUTF();

                if (!user.equals(username)) {
                    tempRaf.writeUTF(user);
                    tempRaf.writeUTF(type);
                    tempRaf.writeUTF(game);
                    tempRaf.writeUTF(trophy);
                    tempRaf.writeUTF(date);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar trofeos: " + e.getMessage());
        }

        new File("psn_trophies").delete();
        new File("psn_trophies_temp").renameTo(new File("psn_trophies"));
    }

    public void addTrophieTo(String username, String trophyGame, String trophyName, Trophy type) throws IOException {
        long pos = users.search(username);
        if (pos == -1L) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        raf.seek(pos);
        String userRead = raf.readUTF();
        boolean isActive = raf.readBoolean();

        if (!isActive) {
            System.out.println("El usuario esta desactivado. No se pueden agregar trofeos.");
            return;
        }

        raf.seek(pos + userRead.length() + 3);
        int points = raf.readInt();
        int trophies = raf.readInt();
        points += type.getPoints();
        trophies++;

        raf.seek(pos + userRead.length() + 3);
        raf.writeInt(points);
        raf.writeInt(trophies);

        try (RandomAccessFile rafTrophies = new RandomAccessFile("psn.tro", "rw")) {
            rafTrophies.seek(rafTrophies.length());
            rafTrophies.writeUTF(username);
            rafTrophies.writeUTF(type.name());
            rafTrophies.writeUTF(trophyGame);
            rafTrophies.writeUTF(trophyName);
            rafTrophies.writeUTF(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        }
    }

    public void playerInfo(String username) throws IOException {
        long pos = users.search(username);
        if (pos == -1L) {
            System.out.println("Usuario no encontrado.");
            return;
        }

        raf.seek(pos);
        String userRead = raf.readUTF();
        boolean isActive = raf.readBoolean();
        int points = raf.readInt();
        int trophies = raf.readInt();

        if (!isActive) {
            System.out.println("El usuario está desactivado.");
            return;
        }

        System.out.println("Username: " + userRead);
        System.out.println("Puntos: " + points);
        System.out.println("Trofeos: " + trophies);

        StringBuilder trophyInfo = new StringBuilder();
        try (RandomAccessFile rafTrophies = new RandomAccessFile("psn.tro", "r")) {
            rafTrophies.seek(0);
            System.out.println("Trofeos ganados:");
            while (rafTrophies.getFilePointer() < rafTrophies.length()) {
                String user = rafTrophies.readUTF();
                String type = rafTrophies.readUTF();
                String game = rafTrophies.readUTF();
                String trophy = rafTrophies.readUTF();
                String date = rafTrophies.readUTF();

                if (user.equals(username)) {
                    trophyInfo.append(date).append(" - ").append(type).append(" - ").append(game).append(" - ").append(trophy).append("\n");
                }
            }
        }

        if (trophyInfo.length() > 0) {
            System.out.println(trophyInfo.toString());
        } else {
            System.out.println("No se han ganado trofeos.");
        }
    }

}
