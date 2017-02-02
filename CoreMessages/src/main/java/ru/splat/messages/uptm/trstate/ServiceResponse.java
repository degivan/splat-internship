package ru.splat.messages.uptm.trstate;

/**
 * Created by Дмитрий on 02.02.2017.
 */
public class ServiceResponse<T> {
    private final T result; //?????почему-то это единственное поле

    public ServiceResponse(T result) {
        this.result = result;
    }

    public T getResult() {
        return result;
    }
    //успешность таски
    public boolean isPositive() {
        if (result instanceof Boolean) {
            return (Boolean)result;
        }
        //выяснить у Ильнара (или Ивана)
        if (result instanceof String) {
            return false;
        }
        if (result instanceof Long) {
            return false;
        }
        if (result instanceof Double) {
            return false;
        }
        else return false;

    }


}
