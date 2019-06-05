package sample;

public class IntHolder
{
    public int value;

    IntHolder(int value) {
        this.value = value;
    }

    public void inc()
    {
        value++;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/*class PassByReference
{
    public static void modify(IntHolder i) {
        i.value = 10;
    }

    public static void main(String[] args)
    {
        IntHolder i = new IntHolder(2);
        modify(i);

        System.out.println(i);
    }
}*/