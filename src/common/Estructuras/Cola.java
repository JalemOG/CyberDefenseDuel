package common.Estructuras;

public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalNodo;
    private int size;

    public Cola() {
        this.frente = null;
        this.finalNodo = null;
        this.size = 0;
    }

    // Agrega un elemento al final de la cola
    public void encolar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            finalNodo.setSiguiente(nuevo);
        }
        finalNodo = nuevo;
        size++;
    }

    // Saca y devuelve el primer elemento de la cola
    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.getValor();
        frente = frente.getSiguiente();
        
        if (frente == null) {
            finalNodo = null; // Si la cola quedó vacía
        }
        size--;
        return dato;
    }

    // Retorna el tamaño actual de la cola
    public int size() {
        return size;
    }

    public boolean estaVacia() {
        return frente == null;
    }
    
    // Permite ver el primero sin sacarlo de la cola
    public T verFrente() {
        if (estaVacia()) return null;
        return frente.getValor();
    }
}