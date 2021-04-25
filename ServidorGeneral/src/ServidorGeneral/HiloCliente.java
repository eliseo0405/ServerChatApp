

package ServidorGeneral;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class HiloCliente extends Thread{
    private final Socket socket;  
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;        
    private final Servidor server;
    private String identificador;
    private boolean escuchando;
    
    public HiloCliente(Socket socket,Servidor server) {
        this.server=server;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("Error en la inicialización");
        }
    }
    
    public void desconnectar() {
        try {
            socket.close();
            escuchando=false;
        } catch (IOException ex) {
            System.err.println("Error al cerrar el socket de comunicación.");
        }
    }
    
    public void run() {
        try{
            escuchar();
        } catch (Exception ex) {
            System.err.println("Error al llamar al método readLine.");
        }
        desconnectar();
    }
        
         
    public void escuchar(){        
        escuchando=true;
        while(escuchando){
            try {
                Object aux=objectInputStream.readObject();
                if(aux instanceof LinkedList){
                    ejecutar((LinkedList<String>)aux);
                }
            } catch (Exception e) {                    
                System.err.println("Error al leer lo enviado por el cliente.");
            }
        }
    }
    
    public void ejecutar(LinkedList<String> lista){
        String tipo=lista.get(0);
        switch (tipo) {
            case "Solicitud de Ingreso":
                confirmarConexion(lista.get(1));
                break;
            case "Solicitud de Desconxión":
                confirmarDesConexion();
                break;                
            case "Mensaje":
                String destinatario=lista.get(2);
                server.clientes
                        .stream()
                        .filter(h -> (destinatario.equals(h.getIdentificador())))
                        .forEach((h) -> h.enviarMensaje(lista));
                break;
            default:
                break;
        }
    }
    /**
     * Método para enviar un mensaje al cliente atraves del socket.
     * @param lista
     */        
    private void enviarMensaje(LinkedList<String> lista){
        try {
            objectOutputStream.writeObject(lista);            
        } catch (Exception e) {
            System.err.println("Error al enviar el objeto al cliente.");
        }
    }  
    
    private void confirmarConexion(String identificador) {
        Servidor.correlativo++;
        this.identificador = Servidor.correlativo+" - "+identificador;
        LinkedList<String> lista=new LinkedList<>();
        lista.add("CONEXION_ACEPTADA");
        lista.add(this.identificador);
        lista.addAll(server.getUsuariosConectados());
        enviarMensaje(lista);
        server.agregarLog("\nNuevo cliente: "+this.identificador);
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("NUEVO_USUARIO_CONECTADO");
        auxLista.add(this.identificador);
        server.clientes
                .stream()
                .forEach(cliente -> cliente.enviarMensaje(auxLista));
        server.clientes.add(this);
    }
    
    public String getIdentificador() {
        return identificador;
    }
    
    private void confirmarDesConexion() {
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("USUARIO_DESCONECTADO");
        auxLista.add(this.identificador);
        server.agregarLog("\nEl cliente \""+this.identificador+"\" se ha desconectado.");
        this.desconnectar();
        for(int i=0;i<server.clientes.size();i++){
            if(server.clientes.get(i).equals(this)){
                server.clientes.remove(i);
                break;
            }
        }
        server.clientes
                .stream()
                .forEach(h -> h.enviarMensaje(auxLista));        
    }
}