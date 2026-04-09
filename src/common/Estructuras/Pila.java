package common.Estructuras;

public class Pila<T> {
    private Nodo<T> tope;
    private int size;

    public Pila() {
        this.tope = null;
        this.size = 0;
    }

    // Agrega un elemento a la cima de la pila
    public void apilar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        nuevo.setSiguiente(tope);
        tope = nuevo;
        size++;
    }

    // Saca y devuelve el elemento de la cima de la pila
    public T desapilar() {
        if (estaVacia()) {
            return null;
        }
        T dato = tope.getValor();
        tope = tope.getSiguiente();
        size--;
        return dato;
    }

    // Permite ver el elemento en la cima sin sacarlo
    public T verTope() {
        if (estaVacia()) {
            return null;
        }
        return tope.getValor();
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int getSize() {
        return size;
    }
}