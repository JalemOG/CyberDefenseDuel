package common.Estructuras;

public class ListaDoblementeEnlazada<T> {
    
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamano;

    public ListaDoblementeEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.tamano = 0;
    }

    /**
     * Devuelve el primer nodo de la lista (útil para recorrerla manualmente)
     */
    public Nodo<T> getCabeza() {
        return cabeza;
    }

    /**
     * Agrega un nuevo elemento al final de la lista (como una cola de espera)
     */
    public void agregar(T valor) {
        Nodo<T> nuevoNodo = new Nodo<>(valor);

        if (cabeza == null) {
            // Si la lista está vacía, el nuevo es tanto cabeza como cola
            cabeza = nuevoNodo;
            cola = nuevoNodo;
        } else {
            // Si ya hay elementos, lo conectamos al final
            cola.setSiguiente(nuevoNodo);
            nuevoNodo.setPrevio(cola);
            cola = nuevoNodo;
        }
        tamano++;
    }

    /**
     * Busca y elimina un elemento específico de la lista.
     * Retorna true si lo eliminó, false si no lo encontró.
     */
    public boolean eliminar(T valor) {
        Nodo<T> actual = cabeza;

        while (actual != null) {
            if (actual.getValor().equals(valor)) {
                
                // Caso 1: Es el único elemento de la lista
                if (cabeza == cola) {
                    cabeza = null;
                    cola = null;
                }
                // Caso 2: Es la cabeza
                else if (actual == cabeza) {
                    cabeza = actual.getSiguiente();
                    cabeza.setPrevio(null);
                }
                // Caso 3: Es la cola
                else if (actual == cola) {
                    cola = actual.getPrevio();
                    cola.setSiguiente(null);
                }
                // Caso 4: Está en el medio
                else {
                    actual.getPrevio().setSiguiente(actual.getSiguiente());
                    actual.getSiguiente().setPrevio(actual.getPrevio());
                }

                tamano--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    /**
     * Retorna la cantidad de elementos en la lista
     */
    public int getTamano() {
        return tamano;
    }
    
    /**
     * Verifica si la lista está vacía
     */
    public boolean estaVacia() {
        return cabeza == null;
    }
}