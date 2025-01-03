import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    private static final String STORAGE_DIR = "server_files";

    public static void main(String[] args) throws IOException {
        // Crear el directorio de almacenamiento si no existe
        new File(STORAGE_DIR).mkdir();

        // Crear un pool de 10 hilos
        ExecutorService poolDeHilos = Executors.newFixedThreadPool(10);

        try (ServerSocket ss = new ServerSocket(4999)) {
            while (true) {
                Socket clientSocket = ss.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());
                poolDeHilos.execute(new ClientHandler(clientSocket));  // Asignar un hilo para manejar el cliente
            }
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    // Clase para manejar cada cliente con un hilo separado del pool de hilos
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                while (true) { // Procesa múltiples comandos por conexión
                    String command = in.readLine();
                    if (command == null) {
                        break; // El cliente cerró la conexión
                    }

                    System.out.println("Comando recibido: " + command);

                    if (command.startsWith("UPLOAD")) {
                        handleUpload(command, in); // Maneja la carga de archivos
                        out.println("Archivo subido exitosamente");
                    } else if (command.startsWith("DOWNLOAD")) {
                        handleDownload(command, out); // Maneja la descarga de archivos
                    } else {
                        out.println("Comando no reconocido");
                    }
                }

            } catch (IOException e) {
                System.err.println("Error con el cliente: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close(); // Cerrar el socket del cliente
                } catch (Exception e) {
                    System.err.println("Error al cerrar el socket del cliente: " + e.getMessage());
                }
            }
        }

        private void handleUpload(String command, BufferedReader in) throws IOException {
            String[] partes = command.split(" ", 2);
            if (partes.length < 2) {
                throw new IOException("Comando UPLOAD inválido");
            }

            String fileName = partes[1];
            File file = new File(STORAGE_DIR, fileName);

            try (BufferedWriter fileout = new BufferedWriter(new FileWriter(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("EOF")) {
                        break; // Termina la carga cuando se recibe "EOF"
                    }
                    fileout.write(line);
                    fileout.newLine();
                }
            }
            System.out.println("Archivo recibido: " + fileName);
        }

        private void handleDownload(String command, PrintWriter out) throws IOException {
            String[] partes = command.split(" ", 2);
            if (partes.length < 2) {
                out.println("Comando DOWNLOAD invalido");
                return;
            }
        
            String fileName = partes[1];
            File file = new File(STORAGE_DIR, fileName);
            if (!file.exists()) {
                out.println("Error: archivo no encontrado");
                return;
            }
        
            try (BufferedReader fileIn = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileIn.readLine()) != null) {
                    out.println(line);  // Enviar cada línea del archivo
                }
            }
        
            out.println("EOF");  // Indicar el fin del archivo al cliente
            System.out.println("Archivo enviado: " + fileName);
        }
        
    }
}
