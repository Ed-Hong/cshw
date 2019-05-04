using System;

class Program
{
    public static void Main(string[] args)
    {
        Console.WriteLine("Bonus Programming Assignment: C#");
        Console.WriteLine("Author: Edward Hong");

        Console.WriteLine("Matrix_Multi\n");
        int[,] a = {{10, 5, 20}, {20, 1, 7}, {10, 15, 12}};
        int[,] b = {{31, 24, 32}, {6, 2, 9}, {10, 22, 11}};
        Matrix_Multi(a,b);
        Console.WriteLine();


        Console.WriteLine("MyMathFunction\n");
        MyMathFunction(10, 10, Add);
        MyMathFunction(10, 10, Multiply);
        MyMathFunction(10, 10, Subtract);
        MyMathFunction(10, 10, Divide);
        MyMathFunction(10, 0, Divide);
        Console.WriteLine();

        Console.WriteLine("Prime\n");
        Prime(5, 2);
        Prime(6, 3);
        Prime(11, 2);
        Prime(11.5, 2);
        Console.WriteLine();
    }

    public static void Matrix_Multi(int[,] a, int[,] b) 
    {
        int a_cols = a.GetLength(1);
        int a_rows = a.GetLength(0);
        int b_rows = b.GetLength(0);
        int b_cols = b.GetLength(1);

        if (a_cols != b_rows) {
            Console.WriteLine("Matrices cannot be multiplied together.");
            return;
        }

        int[,] c = new int[a_rows, b_cols];

        for(int i = 0; i < a_rows; i++) {
            for(int j = 0; j < b_cols; j++) {
                for(int k = 0; k < a_cols; k++) {
                    c[i, j] += a[i,k] * b[k,j];
                }
            }
        }

        for(int i = 0; i < a_rows; i++) {
            for(int j = 0; j < b_cols; j++) {
                Console.Write(c[i,j] + " ");
            }
            Console.WriteLine();
        }
    }

    public static int Add(int a, int b) {
        return a + b;
    }

    public static int Subtract(int a, int b) {
        return a - b;
    }

    public static int Multiply(int a, int b) {
        return a * b;
    }

    public static int Divide(int a, int b) {
        if(b == 0) {
            throw new Exception("Cannot Divide by 0!");
        }
        return a / b;
    }

    public static void MyMathFunction(int a, int b, Func<int, int, int> fn) 
    {
        try 
        {
            Console.WriteLine(fn(a,b));
        }
        catch(Exception e) 
        {
            Console.WriteLine(e.Message);
        }
    }

    public static bool Prime(ValueType input, int i) 
    {
        // check if n can be parsed into an integer
        int n = 0;
        if(!int.TryParse(input.ToString(), out n)) 
        {
            Console.WriteLine("This number is not Prime.");
        }

        // 0 and 1 are not prime
        if (n <= 2) 
            return n == 2; 

        if (n % i == 0) 
        { 
            Console.WriteLine(n + " is not a Prime Number"); 
            return false;
        }

        if (i * i > n) 
        {
            Console.WriteLine(n + " is a Prime Number"); 
            return true;
        }
    
        // try next factor
        return Prime(n, i + 1); 
    } 
    
}

