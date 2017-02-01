package ru.splat.Billing.feautures;


public class PunterBallance
{
    private int sum;
    private int punterId;

    @Override
    public String toString()
    {
        return "PunterBallance{" +
                "sum=" + sum +
                ", punterId=" + punterId +
                '}';
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getPunterId() {
        return punterId;
    }

    public void setPunterId(int punterId) {
        this.punterId = punterId;
    }
}
