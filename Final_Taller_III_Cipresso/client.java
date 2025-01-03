import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    public static void main(String[] args) throws IOException {
        try (Socket s = new Socket("localhost", 4999);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Conectado al servidor");
            while (true) {
                System.out.println("Ingrese un comando (UPLOAD/DOWNLOAD <nombre_archivo>): ");
                String command = scanner.nextLine();

                // Enviar el comando al servidor
                out.println(command);

                if (command.startsWith("UPLOAD")) {
                    handleUpload(command, out, in); // Llamada a la carga de archivos
                } else if (command.startsWith("DOWNLOAD")) {
                    handleDownload(command, in); // Llamada a la descarga de archivos
                } else {
                    String response = in.readLine();
                    if (response != null) {
                        System.out.println("Respuesta del servidor: " + response);
                    } else {
                        System.err.println("Error: el servidor no respondió");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error del cliente: " + e.getMessage());
        }
    }

    private static void handleUpload(String command, PrintWriter out, BufferedReader in) throws IOException {
        String[] partes = command.split(" ", 2);
        if (partes.length < 2) {
            System.out.println("Comando UPLOAD inválido");
            return;
        }

        String fileName = partes[1];
        File file = new File(fileName);

        if (!file.exists()) {
            System.err.println("Error: el archivo no existe");
            return;
        }

        try (BufferedReader filein = new BufferedReader(new FileReader(file))) {
            out.println(command);

            String line;
            while ((line = filein.readLine()) != null) {
                out.println(line); // Enviar contenido del archivo
            }
            out.println("EOF"); // Marca el final del archivo
            out.flush();
        }

        String response = in.readLine();
        if (response != null) {
            System.out.println("Respuesta del servidor: " + response);
        } else {
            System.err.println("Error: el servidor no envió una confirmación.");
        }
    }

    private static void handleDownload(String command, BufferedReader in) throws IOException {
        // Definir el directorio donde se guardarán las descargas
        String downloadDir = "downloads";
    
        // Crear el directorio si no existe
        new File(downloadDir).mkdir();
    
        String fileName = command.split(" ", 2)[1];
        // Definir la ruta completa donde se guardará el archivo descargado
        File downloadedFile = new File(downloadDir, fileName);
    
        try (BufferedWriter fileOut = new BufferedWriter(new FileWriter(downloadedFile))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("EOF")) {
                    break;  // Fin de archivo
                }
                fileOut.write(line);
                fileOut.newLine();  // Escribir línea por línea
            }
           // System.out.println("Archivo descargado con éxito: " + downloadedFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al recibir el archivo: " + e.getMessage());
        }
    }
    
    
}
