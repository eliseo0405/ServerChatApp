
package chatGeneral;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import javax.swing.JOptionPane;

public class Cliente extends Thread {
    
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;   
    private final VentanaC ventana;
    private String identificador;
    private boolean list;
    private final String host;
    private final int port;
    
    Cliente(VentanaC ventana, String host, Integer puerto, String nombre) {
        this.ventana=ventana;        
        this.host = host;
        this.port = puerto;
        this.identificador = nombre;
        list = true;
        this.start();
    }
    public void run(){
        try {
            socket = new Socket(host, port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("You are now online");
            this.enviarSolicitudConexion(identificador);
            this.escuchar();
        } catch (UnknownHostException ex) {
            JOptionPane.showMessageDialog(ventana, "No se ha podido establecer conexión\n"
                                                 + "puede que la ip no sea correcta\n"
                                                 + "o que el servidor no esté abierto.\n"
                                                 + "Esta aplicación se cerrará en breve.");
            System.exit(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(ventana, "Conexión fallida, error de Entrada/Salida,\n"
                                                 + "puede que el puerto no sea correcto,\n"
                                                 + "o que el servidor no este corriendo.\n"
                                                 + "Esta aplicación se cerrará en breve.");
            System.exit(0);
        }

    }
    
    public void desconectar(){
        try {
            objectOutputStream.close();
            objectInputStream.close();
            socket.close();  
            list=false;
        } catch (Exception e) {
            System.err.println("Error al cerrar elementos.");
        }
    }
    public void enviarMensaje(String cliente_receptor, String mensaje){
        LinkedList<String> lista=new LinkedList<>();
        //tipo
        lista.add("Alert");
        lista.add(identificador);
        lista.add(cliente_receptor);
        lista.add(mensaje);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje.");
        }
    }
    
    public void escuchar() {
        try {
            while (list) {
                Object aux = objectInputStream.readObject();
                if (aux != null) {
                    if (aux instanceof LinkedList) {
                        ejecutar((LinkedList<String>)aux);
                    } else {
                        System.err.println("Se recibió un Objeto desconocido a través del socket");
                    }
                } else {
                    System.err.println("Se recibió un null a través del socket");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ventana, "La comunicación con el servidor se ha\n"
                                                 + "perdido, este chat tendrá que finalizar");
            System.exit(0);
        }
    }
    public void ejecutar(LinkedList<String> lista){
        String tipo=lista.get(0);
        switch (tipo) {
            case "Conexión Establecida":
                identificador=lista.get(1);
                ventana.sesionIniciada(identificador);
                for(int i=2;i<lista.size();i++){
                    ventana.addContacto(lista.get(i));
                }
                break;
            case "Nuevo Usuario Registrado":
                ventana.addContacto(lista.get(1));
                break;
            case "Usuario Desconectado":
                ventana.eliminarContacto(lista.get(1));
                break;                
            case "Alert":
                ventana.addMensaje(lista.get(1), lista.get(3));
                break;
            default:
                break;
        }
    }
    private void enviarSolicitudConexion(String identificador) {
        LinkedList<String> lista=new LinkedList<>();
        lista.add("Solicitud de ingreso");
        lista.add(identificador);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje.");
        }
    }
    void confirmarDesconexion() {
        LinkedList<String> lista=new LinkedList<>();
        //tipo
        lista.add("Solicitud de Salida");
        //cliente solicitante
        lista.add(identificador);
        try {
            objectOutputStream.writeObject(lista);
        } catch (IOException ex) {
            System.out.println("Error de lectura y escritura al enviar mensaje.");
        }
    }
    String getIdentificador() {
        return identificador;
    }
}