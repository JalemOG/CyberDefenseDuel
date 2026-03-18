package common.Estructuras;

public class ListaDoblementeEnlazada<T> {
    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int size;

    public ListaDoblementeEnlazada() {
        this.cabeza = null;
        this.cola = null;
        this.size = 0;
    }

    public int getCount() { return size; }
    public Nodo<T> getCabeza() { return cabeza; }
    public Nodo<T> getCola() { return cola; }

    public void append(T value) {
        Nodo<T> nodo = new Nodo<>(value);
        if (cola == null) {
            cabeza = nodo;
            cola = nodo;
        } else {
            nodo.setPrevio(cola);
            cola.setSiguiente(nodo);
            cola = nodo;
        }
        size++;
    }

    public void prepend(T value) {
        Nodo<T> nodo = new Nodo<>(value);
        if (cabeza == null) {
            cabeza = nodo;
            cola = nodo;
        } else {
            nodo.setSiguiente(cabeza);
            cabeza.setPrevio(nodo);
            cabeza = nodo;
        }
        size++;
    }

    public void clear() {
        cabeza = null;
        cola = null;
        size = 0;
    }
}