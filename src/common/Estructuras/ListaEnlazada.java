package common.Estructuras;

public class ListaEnlazada<T> {
    private Nodo<T> cabeza;
    private int size;

    public ListaEnlazada() {
        this.cabeza = null;
        this.size = 0;
    }

    // Agrega un elemento al final de la lista
    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        size++;
    }

    // Obtiene un elemento en una posición específica (usado para iterar al guardar)
    public T obtener(int indice) {
        if (indice < 0 || indice >= size) {
            return null;
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente();
        }
        return actual.getValor();
    }

    // Elimina un elemento específico
    public boolean eliminar(T elemento) {
        if (cabeza == null) return false;
        
        if (cabeza.getValor().equals(elemento)) {
            cabeza = cabeza.getSiguiente();
            size--;
            return true;
        }
        
        Nodo<T> actual = cabeza;
        while (actual.getSiguiente() != null) {
            if (actual.getSiguiente().getValor().equals(elemento)) {
                actual.setSiguiente(actual.getSiguiente().getSiguiente());
                size--;
                return true;
            }
            actual = actual.getSiguiente();
        }
        return false;
    }

    // Retorna la cantidad actual de elementos
    public int getSize() {
        return size;
    }

    public boolean estaVacia() {
        return size == 0;
    }
}